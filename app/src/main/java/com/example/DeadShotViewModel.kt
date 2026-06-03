package com.example

import android.content.Context
import android.os.Environment
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File
import kotlin.random.Random

enum class ActiveScreen {
    SPLASH, LOGIN, LOBBY, ARENA
}

enum class PakistanMap(val label: String, val locationName: String, val desc: String) {
    KARACHI("Karachi Port", "Saddar Docks", "Industrial shipping containers, dangling cranes, and harbor targets."),
    LAHORE("Lahore Fort", "Dewan-e-Aam", "Ancient Mughal architecture, red sandstone arches, and wooden targets."),
    ISLAMABAD("Islamabad Hills", "Margalla Heights", "High altitude outpost, pine forestry, and rail-moving targets.")
}

enum class WeatherType(val label: String, val desc: String) {
    SUNNY("Sunny Sky", "High precision, minimal recoil draft, and beautiful sun flares."),
    MONSOON("Monsoon Rain", "Falling precipitation particles, lightning flash, and higher sway."),
    SANDSTORM("Dust Sandstorm", "Orange desert dust fog, heavy low visibility windage draft.")
}

enum class WeaponType(val label: String, val ammoMax: Int, val fireRateMs: Long, val damage: Int, val description: String) {
    DEAGLE("Desert Eagle .50 AE", 7, 600, 35, "Heavy tactical handgun with massive vertical kickback."),
    M4A1("M4A1-S Carbine", 30, 120, 15, "Full-auto rifle with moderate recoil and stable precision."),
    AWP("AWP Sniper Rifle", 5, 1200, 100, "Bolt-action sniper with scope zoom and one-shot destructibility.")
}

enum class CameraAngle(val label: String) {
    FIRST_PERSON("FPS Eye-Level"),
    OVER_THE_SHOULDER("Combat Cam"),
    DRONE("Drone Tactical")
}

data class TargetItem(
    val id: Int,
    var x: Float,
    var y: Float,
    val size: Float,
    var hp: Int,
    val maxHp: Int,
    var isDestroyed: Boolean = false,
    val speedX: Float = 0f,
    val speedY: Float = 0f,
    var lastCrackSeed: Int = 0
)

data class ChatMessage(
    val senderName: String,
    val content: String,
    val id: Long = System.currentTimeMillis() + Random.nextLong(100),
    val isUser: Boolean = false
)

data class SystemMail(
    val id: Int,
    val title: String,
    val text: String,
    val rubyBonus: Int,
    var isRead: Boolean = false,
    var isClaimed: Boolean = false
)

class DeadShotViewModel : ViewModel() {

    // Screens navigation
    private val _currentScreen = MutableStateFlow(ActiveScreen.SPLASH)
    val currentScreen: StateFlow<ActiveScreen> = _currentScreen.asStateFlow()

    // Authentication States
    private val _isGuestUser = MutableStateFlow(false)
    val isGuestUser: StateFlow<Boolean> = _isGuestUser.asStateFlow()

    private val _profileName = MutableStateFlow("Guest_DS")
    val profileName: StateFlow<String> = _profileName.asStateFlow()

    private val _playerBId = MutableStateFlow(0)
    val playerBId: StateFlow<Int> = _playerBId.asStateFlow()

    private val _showPermissionDialog = MutableStateFlow(false)
    val showPermissionDialog: StateFlow<Boolean> = _showPermissionDialog.asStateFlow()

    private val _googleAccounts = MutableStateFlow<List<String>>(emptyList())
    val googleAccounts: StateFlow<List<String>> = _googleAccounts.asStateFlow()

    private val _showGoogleAccountPicker = MutableStateFlow(false)
    val showGoogleAccountPicker: StateFlow<Boolean> = _showGoogleAccountPicker.asStateFlow()

    private val _googleDriveSyncState = MutableStateFlow("") // "", "SYNCING", "COMPLETED"
    val googleDriveSyncState: StateFlow<String> = _googleDriveSyncState.asStateFlow()

    private val _folderPathStatus = MutableStateFlow("No folder created yet")
    val folderPathStatus: StateFlow<String> = _folderPathStatus.asStateFlow()

    private val _showProfileNameDialog = MutableStateFlow(false)
    val showProfileNameDialog: StateFlow<Boolean> = _showProfileNameDialog.asStateFlow()

    // Currency state
    private val _rubies = MutableStateFlow(3500) // Double as DSG for backward compatibility if needed
    val rubies: StateFlow<Int> = _rubies.asStateFlow()

    private val _dsgCurrency = MutableStateFlow(3500) // DSG Free Currency (Dead Shot Gems)
    val dsgCurrency: StateFlow<Int> = _dsgCurrency.asStateFlow()

    // Weapon Skins State
    private val _equippedWeaponSkins = MutableStateFlow(mutableMapOf("M4A1" to "Classic Slate", "AWP" to "Classic Slate", "DEAGLE" to "Classic Slate"))
    val equippedWeaponSkins: StateFlow<Map<String, String>> = _equippedWeaponSkins.asStateFlow()

    private val _unlockedWeaponSkins = MutableStateFlow(listOf("Classic Slate", "Neon Viper M4A1", "Crimson Drake AWP", "Luxe Shahi Deagle"))
    val unlockedWeaponSkins: StateFlow<List<String>> = _unlockedWeaponSkins.asStateFlow()

    // Character Presets State
    private val _equippedCharacter = MutableStateFlow("DJ Alok") // Free Fire DJ Alok, Chrono, Kelly
    val equippedCharacter: StateFlow<String> = _equippedCharacter.asStateFlow()

    private val _unlockedCharacters = MutableStateFlow(listOf("DJ Alok", "Kelly", "Chrono"))
    val unlockedCharacters: StateFlow<List<String>> = _unlockedCharacters.asStateFlow()

    // Active Character skills
    private val _isSkillActive = MutableStateFlow(false)
    val isSkillActive: StateFlow<Boolean> = _isSkillActive.asStateFlow()

    private val _skillCooldown = MutableStateFlow(0) // Seconds remaining
    val skillCooldown: StateFlow<Int> = _skillCooldown.asStateFlow()

    // Skill-Slot system state variables representing equipped active & passive skills
    private val _equippedActiveSkill = MutableStateFlow("Drop the Beat")
    val equippedActiveSkill: StateFlow<String> = _equippedActiveSkill.asStateFlow()

    private val _equippedPassiveSkill = MutableStateFlow("Andrew's Vest Protection")
    val equippedPassiveSkill: StateFlow<String> = _equippedPassiveSkill.asStateFlow()

    private val _mocoTaggedTargetId = MutableStateFlow<Int?>(null)
    val mocoTaggedTargetId: StateFlow<Int?> = _mocoTaggedTargetId.asStateFlow()

    // Real-time Survivors Matchmaking Simulator
    private val _survivorsCount = MutableStateFlow(50)
    val survivorsCount: StateFlow<Int> = _survivorsCount.asStateFlow()

    private val _arenaKills = MutableStateFlow(0)
    val arenaKills: StateFlow<Int> = _arenaKills.asStateFlow()

    private val _eliminationBadge = MutableStateFlow<String?>(null)
    val eliminationBadge: StateFlow<String?> = _eliminationBadge.asStateFlow()

    private val _battleLogFeed = MutableStateFlow<List<String>>(emptyList())
    val battleLogFeed: StateFlow<List<String>> = _battleLogFeed.asStateFlow()

    // Mail State
    private val _mailList = MutableStateFlow<List<SystemMail>>(emptyList())
    val mailList: StateFlow<List<SystemMail>> = _mailList.asStateFlow()

    // Team Lobby Chat Message State
    private val _chatMessages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val chatMessages: StateFlow<List<ChatMessage>> = _chatMessages.asStateFlow()

    // Friends list with game activity statuses
    private val _friendsList = MutableStateFlow<List<Map<String, String>>>(emptyList())
    val friendsList: StateFlow<List<Map<String, String>>> = _friendsList.asStateFlow()

    // Game Configurations
    private val _selectedMap = MutableStateFlow(PakistanMap.KARACHI)
    val selectedMap: StateFlow<PakistanMap> = _selectedMap.asStateFlow()

    private val _selectedWeather = MutableStateFlow(WeatherType.SUNNY)
    val selectedWeather: StateFlow<WeatherType> = _selectedWeather.asStateFlow()

    private val _selectedWeapon = MutableStateFlow(WeaponType.M4A1)
    val selectedWeapon: StateFlow<WeaponType> = _selectedWeapon.asStateFlow()

    private val _activeCameraAngle = MutableStateFlow(CameraAngle.FIRST_PERSON)
    val activeCameraAngle: StateFlow<CameraAngle> = _activeCameraAngle.asStateFlow()

    // NEW GAME ELEMENTS (Modes, Outfits, HUD, Parachuting)
    private val _selectedGameMode = MutableStateFlow("SOLO") // "SOLO", "DUO", "SQUAD"
    val selectedGameMode: StateFlow<String> = _selectedGameMode.asStateFlow()

    private val _equippedOutfit = MutableStateFlow("Standard Outfit")
    val equippedOutfit: StateFlow<String> = _equippedOutfit.asStateFlow()

    private val _unlockedOutfits = MutableStateFlow(listOf("Standard Outfit", "Tactical Camo", "Golden Elite Armor"))
    val unlockedOutfits: StateFlow<List<String>> = _unlockedOutfits.asStateFlow()

    private val _isDailyLimitAngelBundlePurchased = MutableStateFlow(false)
    val isDailyLimitAngelBundlePurchased: StateFlow<Boolean> = _isDailyLimitAngelBundlePurchased.asStateFlow()

    // Settings Parameters (HUD scaling, Sensitivity, Red dot size, sound eras)
    private val _sensitivityVal = MutableStateFlow(1.0f)
    val sensitivityVal: StateFlow<Float> = _sensitivityVal.asStateFlow()

    private val _redDotSizeVal = MutableStateFlow(10f)
    val redDotSizeVal: StateFlow<Float> = _redDotSizeVal.asStateFlow()

    private val _joystickScaleVal = MutableStateFlow(1.0f)
    val joystickScaleVal: StateFlow<Float> = _joystickScaleVal.asStateFlow()

    private val _fireButtonScaleVal = MutableStateFlow(1.0f)
    val fireButtonScaleVal: StateFlow<Float> = _fireButtonScaleVal.asStateFlow()

    private val _retroSoundOn = MutableStateFlow(true)
    val retroSoundOn: StateFlow<Boolean> = _retroSoundOn.asStateFlow()

    // Interactive Plane Drop and Glider Parachute State
    private val _parachuteDropHeight = MutableStateFlow(200f) // height counts from 200m down to 0m
    val parachuteDropHeight: StateFlow<Float> = _parachuteDropHeight.asStateFlow()

    private val _isParachuteGlidePhase = MutableStateFlow(false) // Triggered from Deploy/Start button
    val isParachuteGlidePhase: StateFlow<Boolean> = _isParachuteGlidePhase.asStateFlow()

    private val _isParachuteOpened = MutableStateFlow(false) // Auto opens below 50m
    val isParachuteOpened: StateFlow<Boolean> = _isParachuteOpened.asStateFlow()

    // Firing Arena States
    private val _currentAmmo = MutableStateFlow(WeaponType.M4A1.ammoMax)
    val currentAmmo: StateFlow<Int> = _currentAmmo.asStateFlow()

    private val _isReloading = MutableStateFlow(false)
    val isReloading: StateFlow<Boolean> = _isReloading.asStateFlow()

    private val _isFiring = MutableStateFlow(false)
    val isFiring: StateFlow<Boolean> = _isFiring.asStateFlow()

    private val _recoilOffset = MutableStateFlow(0f) // Recoil displacement animation
    val recoilOffset: StateFlow<Float> = _recoilOffset.asStateFlow()

    private val _isScoped = MutableStateFlow(false) // Sniper Zoom
    val isScoped: StateFlow<Boolean> = _isScoped.asStateFlow()

    private val _scenicTargets = MutableStateFlow<List<TargetItem>>(emptyList())
    val scenicTargets: StateFlow<List<TargetItem>> = _scenicTargets.asStateFlow()

    private val _score = MutableStateFlow(0)
    val score: StateFlow<Int> = _score.asStateFlow()

    private val _floatingScores = MutableStateFlow<List<Pair<Float, Float>>>(emptyList()) // x, y offset coordinates of scores

    // GARENA FREE FIRE SYSTEM INTEGRATIONS
    private val _playerHp = MutableStateFlow(200)
    val playerHp: StateFlow<Int> = _playerHp.asStateFlow()

    private val _playerEp = MutableStateFlow(100)
    val playerEp: StateFlow<Int> = _playerEp.asStateFlow()

    private val _glooWallCount = MutableStateFlow(3)
    val glooWallCount: StateFlow<Int> = _glooWallCount.asStateFlow()

    private val _isGlooWallDeployed = MutableStateFlow(false)
    val isGlooWallDeployed: StateFlow<Boolean> = _isGlooWallDeployed.asStateFlow()

    private val _glooWallHp = MutableStateFlow(150)
    val glooWallHp: StateFlow<Int> = _glooWallHp.asStateFlow()

    private val _glooWallX = MutableStateFlow(400f)
    val glooWallX: StateFlow<Float> = _glooWallX.asStateFlow()

    private val _glooWallY = MutableStateFlow(240f)
    val glooWallY: StateFlow<Float> = _glooWallY.asStateFlow()

    private val _glooWallSecondsLeft = MutableStateFlow(0)
    val glooWallSecondsLeft: StateFlow<Int> = _glooWallSecondsLeft.asStateFlow()

    private val _medkitCount = MutableStateFlow(3)
    val medkitCount: StateFlow<Int> = _medkitCount.asStateFlow()

    private val _isChannelingHeal = MutableStateFlow(false)
    val isChannelingHeal: StateFlow<Boolean> = _isChannelingHeal.asStateFlow()

    private val _healProgress = MutableStateFlow(0f)
    val healProgress: StateFlow<Float> = _healProgress.asStateFlow()

    private val _mushroomX = MutableStateFlow(320f)
    val mushroomX: StateFlow<Float> = _mushroomX.asStateFlow()
    private val _mushroomY = MutableStateFlow(220f)
    val mushroomY: StateFlow<Float> = _mushroomY.asStateFlow()
    private val _isMushroomVisible = MutableStateFlow(true)
    val isMushroomVisible: StateFlow<Boolean> = _isMushroomVisible.asStateFlow()

    private val _airDropX = MutableStateFlow(420f)
    val airDropX: StateFlow<Float> = _airDropX.asStateFlow()
    private val _airDropY = MutableStateFlow(-100f)
    val airDropY: StateFlow<Float> = _airDropY.asStateFlow()
    private val _isAirDropInbound = MutableStateFlow(false)
    val isAirDropInbound: StateFlow<Boolean> = _isAirDropInbound.asStateFlow()
    private val _isAirDropLanded = MutableStateFlow(false)
    val isAirDropLanded: StateFlow<Boolean> = _isAirDropLanded.asStateFlow()
    private val _isAirDropLooted = MutableStateFlow(false)
    val isAirDropLooted: StateFlow<Boolean> = _isAirDropLooted.asStateFlow()

    private val _isBooyahActive = MutableStateFlow(false)
    val isBooyahActive: StateFlow<Boolean> = _isBooyahActive.asStateFlow()

    // Deploy Gloo Wall at custom crosshair position
    fun deployGlooWall(x: Float = 400f, y: Float = 240f): Boolean {
        if (_glooWallCount.value > 0 && !_isGlooWallDeployed.value) {
            _glooWallCount.value -= 1
            _isGlooWallDeployed.value = true
            _glooWallHp.value = 150
            _glooWallX.value = x
            _glooWallY.value = y
            
            // Add to battle logs
            val logs = _battleLogFeed.value.toMutableList()
            logs.add("🛡️ ${_profileName.value} DEPLOYED GLOO WALL! SHIELD EQUIPPED AT CORE AREA!")
            _battleLogFeed.value = logs
            
            // Timer to automatically melt the wall after 10 seconds
            viewModelScope.launch {
                for (i in 10 downTo 1) {
                    _glooWallSecondsLeft.value = i
                    delay(1000)
                }
                _glooWallSecondsLeft.value = 0
                if (_isGlooWallDeployed.value) {
                    _isGlooWallDeployed.value = false
                    val logsNew = _battleLogFeed.value.toMutableList()
                    logsNew.add("❄️ GLOO WALL MELTED DOWN AFTER 10S!")
                    _battleLogFeed.value = logsNew
                }
            }
            return true
        }
        return false
    }

    // Eat Mushroom
    fun consumeMushroom(): Boolean {
        if (_isMushroomVisible.value) {
            _isMushroomVisible.value = false
            _playerEp.value = 100
            val logs = _battleLogFeed.value.toMutableList()
            logs.add("🍄 ${_profileName.value} ATE LEVEL-4 MUSHROOM (+100 EP)!")
            _battleLogFeed.value = logs
            return true
        }
        return false
    }

    // Loot Air Drop
    fun lootAirDrop(): Boolean {
        if (_isAirDropLanded.value && !_isAirDropLooted.value) {
            _isAirDropLooted.value = true
            _glooWallCount.value += 2
            _medkitCount.value = (_medkitCount.value + 2).coerceAtMost(5)
            _playerHp.value = 200
            _playerEp.value = 100
            addDsgCurrency(500)
            
            val logs = _battleLogFeed.value.toMutableList()
            logs.add("🎁 ${_profileName.value} LOOTED HEAVEN AIRDROP (+500 DSG, +2 GLOO, +2 MEDKITS)")
            _battleLogFeed.value = logs
            return true
        }
        return false
    }

    fun getMaxHp(): Int {
        return if (_equippedPassiveSkill.value == "Andrew's Vest Protection") 240 else 200
    }

    fun equipActiveSkill(skillName: String) {
        _equippedActiveSkill.value = skillName
        val logs = _battleLogFeed.value.toMutableList()
        logs.add("⚙️ SKILL UNLOCKED: Equipped Active Skill: ${skillName.uppercase()}!")
        if (logs.size > 5) logs.removeAt(0)
        _battleLogFeed.value = logs
    }

    fun equipPassiveSkill(skillName: String) {
        _equippedPassiveSkill.value = skillName
        val maxLimitHp = getMaxHp()
        if (skillName == "Andrew's Vest Protection") {
            _playerHp.value = maxLimitHp
        } else {
            _playerHp.value = _playerHp.value.coerceAtMost(200)
        }
        val logs = _battleLogFeed.value.toMutableList()
        logs.add("⚙️ SKILL UNLOCKED: Equipped Passive Skill: ${skillName.uppercase()}!")
        if (logs.size > 5) logs.removeAt(0)
        _battleLogFeed.value = logs
    }

    // Perform Medkit treatment
    fun useMedkit(): Boolean {
        val maxHpLimit = getMaxHp()
        if (_medkitCount.value > 0 && !_isChannelingHeal.value && _playerHp.value < maxHpLimit) {
            _isChannelingHeal.value = true
            _healProgress.value = 0f
            
            val logs = _battleLogFeed.value.toMutableList()
            logs.add("🩹 ${_profileName.value} IS ENGAGING MEDKIT TREATMENT...")
            _battleLogFeed.value = logs
            
            viewModelScope.launch {
                val isMaxim = _equippedPassiveSkill.value == "Maxim's Gluttony"
                val totalDurationMs = if (isMaxim) 1500 else 3000
                val intervalMs = 100
                val steps = totalDurationMs / intervalMs
                for (s in 1..steps) {
                    delay(intervalMs.toLong())
                    if (!_isChannelingHeal.value) return@launch
                    _healProgress.value = s.toFloat() / steps
                }
                
                _isChannelingHeal.value = false
                _healProgress.value = 0f
                _medkitCount.value -= 1
                
                val hpHealed = (_playerHp.value + 75).coerceAtMost(getMaxHp())
                _playerHp.value = hpHealed
                
                val finalLogs = _battleLogFeed.value.toMutableList()
                finalLogs.add("💚 COMPLETED TREATMENT! RECOVERED +75 HP!")
                _battleLogFeed.value = finalLogs
            }
            return true
        }
        return false
    }

    // Trigger Free Fire Style Quick Character Voice phrases
    fun triggerQuickVoice(phraseType: String) {
        val currentLogs = _battleLogFeed.value.toMutableList()
        val text = when (phraseType) {
            "SPOTTED" -> "🎯 ${_profileName.value.uppercase()}: ENEMY SPOTTED!"
            "HEAL" -> "🩹 ${_profileName.value.uppercase()}: I NEED MEDKITS!"
            "GLOO" -> "🛡️ ${_profileName.value.uppercase()}: HOID COVER GLOO SHIELD UP!"
            "ZONE" -> "🏃 ${_profileName.value.uppercase()}: SAFE ZONE IS SHRINKING!"
            "COVER" -> "🔫 ${_profileName.value.uppercase()}: PROVIDE COVER FIRE!"
            else -> ""
        }
        if (text.isNotEmpty()) {
            currentLogs.add(text)
            if (currentLogs.size > 5) currentLogs.removeAt(0)
            _battleLogFeed.value = currentLogs
            
            // Simulating multiplayer teammate reply
            viewModelScope.launch {
                delay(1200)
                val botRespLogs = _battleLogFeed.value.toMutableList()
                val bots = listOf("Alok_Pro", "Kelly_Agile", "Chrono_Shield", "Muga_Bot")
                val botName = bots.random()
                val botText = when (phraseType) {
                    "SPOTTED" -> "⚔️ $botName: Location marked, pushing forward!"
                    "HEAL" -> "🩹 $botName: Take some form of supply crates!"
                    "GLOO" -> "🛡️ $botName: Shield is secure, hold position!"
                    "ZONE" -> "🏃 $botName: Roger that, moving to safe sector!"
                    "COVER" -> "🔥 $botName: Firing heavy cover, rush them!"
                    else -> ""
                }
                if (botText.isNotEmpty()) {
                    botRespLogs.add(botText)
                    if (botRespLogs.size > 5) botRespLogs.removeAt(0)
                    _battleLogFeed.value = botRespLogs
                }
            }
        }
    }

    // Trigger target shot checking for Mushroom and Airdrop
    fun checkAuxiliaryShots(xImpact: Float, yImpact: Float): String {
        // Check if hit mushroom
        if (_isMushroomVisible.value) {
            val distSqr = (xImpact - _mushroomX.value) * (xImpact - _mushroomX.value) + (yImpact - _mushroomY.value) * (yImpact - _mushroomY.value)
            if (distSqr <= 30f * 30f) {
                consumeMushroom()
                return "🍄 LEVEL 4 MUSHROOM CONSUMED! EP RECOVERY MAXED!"
            }
        }
        // Check if hit airdrop
        if (_isAirDropLanded.value && !_isAirDropLooted.value) {
            val distSqr = (xImpact - _airDropX.value) * (xImpact - _airDropX.value) + (yImpact - _airDropY.value) * (yImpact - _airDropY.value)
            if (distSqr <= 50f * 50f) {
                lootAirDrop()
                return "🎁 HEAVENLY AIRDROP OPENED! GOT VEST LEVEL 4 & GROZA AMMO +500 DSG!"
            }
        }
        return ""
    }

    // Damage player
    fun damagePlayer(damage: Int, onMessage: (String) -> Unit) {
        if (_isBooyahActive.value) return
        if (_isGlooWallDeployed.value) {
            val currentGlooHp = _glooWallHp.value - damage
            if (currentGlooHp <= 0) {
                _isGlooWallDeployed.value = false
                _glooWallHp.value = 0
                onMessage("💥 GLOO WALL COLLAPSED UNDER ATTACK!")
            } else {
                _glooWallHp.value = currentGlooHp
                onMessage("🛡️ GLOO WALL ABSORBED IMPACT! (Gloo HP: $currentGlooHp)")
            }
        } else if (_isSkillActive.value && _equippedCharacter.value == "Chrono") {
            onMessage("🛡️ CHRONO TIME-BARRIER SHIELD BLOCKED THE HIT!")
        } else {
            // Apply kelly shift or body impact
            val isKellyActive = _isSkillActive.value && _equippedCharacter.value == "Kelly"
            if (isKellyActive && Random.nextFloat() < 0.50f) {
                onMessage("💨 KELLY AGILITY SPLIT-SECOND SHIFT DODGED!")
            } else {
                val finalHp = (_playerHp.value - damage).coerceAtLeast(0)
                _playerHp.value = finalHp
                if (finalHp <= 0) {
                    onMessage("☠️ TEAMMATE REVIVED YOU! AUTODEPOSIT GLOOWALL (+1 GLOO)")
                    _playerHp.value = 100
                    _glooWallCount.value += 1
                } else {
                    onMessage("💥 RIVAL HIT YOU! GLOO WALL UP NOW! -$damage HP")
                }
            }
        }
    }

    fun forceBooyah() {
        if (!_isBooyahActive.value) {
            _isBooyahActive.value = true
            addDsgCurrency(1000)
        }
    }

    init {
        // Run splash countdown on start
        viewModelScope.launch {
            delay(2800) // Beautiful 2.8 second splash
            _currentScreen.value = ActiveScreen.LOGIN
        }

        // Initialize mail elements
        _mailList.value = listOf(
            SystemMail(1, "Welcome Soldier", "Welcome to Dead Shot, the ultimate landscape first person tactical shooter! Dynamic atmospheric battles await you.", 300),
            SystemMail(2, "Lahore Battle Suite", "New testing weaponry suite unlocked for the Lahore Fort Mughal ruins combat sandbox.", 500),
            SystemMail(3, "Monsoon Weather Update", "Heavy monsoons trigger increased weapon drift. Maximize scoping techniques.", 200)
        )

        // Initialize friends
        _friendsList.value = listOf(
            mapOf("name" to "KarachiKnight", "bId" to "Bid_23984", "status" to "Lobby", "weapon" to "M4A1"),
            mapOf("name" to "MargallaSniper", "bId" to "Bid_66319", "status" to "In-Match: Islamabad", "weapon" to "AWP"),
            mapOf("name" to "RaviWarrior", "bId" to "Bid_44122", "status" to "In-Match: Lahore", "weapon" to "Deagle"),
            mapOf("name" to "AnimeRage", "bId" to "Bid_89112", "status" to "Offline", "weapon" to "M4A1")
        )

        // Populate introductory chat messages
        _chatMessages.value = listOf(
            ChatMessage("MargallaSniper", "Who wants to practice scope challenges in Islamabad Heights?"),
            ChatMessage("KarachiKnight", "Always down, invite me when lobby is ready!"),
            ChatMessage("RaviWarrior", "Recoil on Desert Eagle feels incredibly realistic in Sunny weather!")
        )

        // Start chat simulation loop
        simulateTeammateChats()
    }

    fun navigateTo(screen: ActiveScreen) {
        _currentScreen.value = screen
    }

    fun selectMap(map: PakistanMap) {
        _selectedMap.value = map
        if (_currentScreen.value == ActiveScreen.ARENA) {
            setupArenaTargets()
        }
    }

    fun selectWeather(weather: WeatherType) {
        _selectedWeather.value = weather
    }

    fun selectWeapon(weapon: WeaponType) {
        _selectedWeapon.value = weapon
        _currentAmmo.value = weapon.ammoMax
        _isScoped.value = false
    }

    fun toggleCameraAngle() {
        val nextIndex = (CameraAngle.values().indexOf(_activeCameraAngle.value) + 1) % CameraAngle.values().size
        _activeCameraAngle.value = CameraAngle.values()[nextIndex]
    }

    fun toggleScope() {
        if (_selectedWeapon.value == WeaponType.AWP) {
            _isScoped.value = !_isScoped.value
        }
    }

    // GUEST FLOW
    fun onGuestLoginClicked() {
        _showPermissionDialog.value = true
    }

    fun dismissPermissionDialog() {
        _showPermissionDialog.value = false
    }

    fun grantStoragePermission(context: Context) {
        _showPermissionDialog.value = false
        _isGuestUser.value = true
        
        // Generate values
        val randomNum = Random.nextInt(100000, 999999)
        _playerBId.value = randomNum
        _profileName.value = "Guest_$randomNum"

        viewModelScope.launch(Dispatchers.IO) {
            try {
                // Fulfill literal path requirement "com.dead.shot" folder creation
                val externalRootDir = Environment.getExternalStorageDirectory()
                val targetFolder = File(externalRootDir, "com.dead.shot")
                
                var success = false
                var pathUsed = ""
                
                try {
                    if (!targetFolder.exists()) {
                        success = targetFolder.mkdirs()
                    } else {
                        success = true
                    }
                    pathUsed = targetFolder.absolutePath
                } catch (e: Exception) {
                    Log.e("DeadShot", "Couldn't write to external root (restricted on modern Android). Fallback to standard folders.")
                }

                // If sdcard root is blocked (Android 11+ scoped storage), create it in Shared Documents or standard Sandbox
                if (!success) {
                    val fallbackPublicDir = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS), "com.dead.shot")
                    if (!fallbackPublicDir.exists()) {
                        success = fallbackPublicDir.mkdirs()
                    } else {
                        success = true
                    }
                    pathUsed = fallbackPublicDir.absolutePath
                }

                // App sandbox fallback (guaranteed to succeed and readable!)
                val sandboxFolder = File(context.filesDir, "com.dead.shot")
                if (!sandboxFolder.exists()) {
                    sandboxFolder.mkdirs()
                }
                
                // Write guest identity to files
                val identityFileInSandbox = File(sandboxFolder, "guest_identity.txt")
                identityFileInSandbox.writeText("BId=$_playerBId\nProfileName=${_profileName.value}\nLoginType=GUEST")

                if (success) {
                    val publicIdentityFile = File(pathUsed, "guest_identity.txt")
                    try {
                        publicIdentityFile.writeText("BId=$_playerBId\nProfileName=${_profileName.value}\nLoginType=GUEST")
                        _folderPathStatus.value = "Folder successfully written at: $pathUsed \nFile: guest_identity.txt"
                    } catch (e: Exception) {
                        _folderPathStatus.value = "Written inside Sandboxed path: ${sandboxFolder.absolutePath}"
                    }
                } else {
                    _folderPathStatus.value = "Written inside Sandboxed path: ${sandboxFolder.absolutePath}"
                }
            } catch (e: Exception) {
                Log.e("DeadShot", "Error creating directories", e)
                _folderPathStatus.value = "Fallback Active: Sandbox ID generated."
            }
        }
    }

    // GOOGLE FLOW
    fun onGoogleLoginClicked(userEmail: String?) {
        _googleAccounts.value = listOf(
            userEmail ?: "hermanrameez@gmail.com",
            "deadshot.pro@gmail.com",
            "tactics.gamer@gmail.com"
        )
        _showGoogleAccountPicker.value = true
    }

    fun selectGoogleAccount(account: String) {
        _showGoogleAccountPicker.value = false
        _googleDriveSyncState.value = "SYNCING"
        
        viewModelScope.launch {
            // Simulate realistic Google Drive authorization and upload/save loading of data
            delay(2000)
            _googleDriveSyncState.value = "COMPLETED"
            
            // Assign pre-existing Google credentials or open custom name picker
            val extractedName = account.substringBefore("@")
            _profileName.value = extractedName
            _playerBId.value = Random.nextInt(100000, 999999)
            _isGuestUser.value = false
            
            _showProfileNameDialog.value = true
        }
    }

    fun submitProfileName(name: String) {
        if (name.isNotEmpty()) {
            _profileName.value = name
        }
        _showProfileNameDialog.value = false
    }

    fun dismissGooglePicker() {
        _showGoogleAccountPicker.value = false
    }

    // FACEBOOK LOGIN
    fun performFacebookLogin() {
        val fbBId = Random.nextInt(100000, 999999)
        _profileName.value = "Fighter_$fbBId"
        _playerBId.value = fbBId
        _isGuestUser.value = false
        _folderPathStatus.value = "Facebook Account Profile Authenticated."
    }

    fun addRubies(amount: Int) {
        _dsgCurrency.value += amount
        _rubies.value = _dsgCurrency.value
    }

    fun addDsgCurrency(amount: Int) {
        _dsgCurrency.value += amount
        _rubies.value = _dsgCurrency.value
    }

    fun changeWeaponSkin(weapon: String, skinName: String) {
        val current = _equippedWeaponSkins.value.toMutableMap()
        current[weapon] = skinName
        _equippedWeaponSkins.value = current
    }

    fun equipCharacter(characterName: String) {
        if (_unlockedCharacters.value.contains(characterName)) {
            _equippedCharacter.value = characterName
        }
    }

    // Spend 100 DSG to draw an ultra exclusive skin or suit from Luck Royale!
    fun spinLuckRoyale(onPrizeGranted: (String) -> Unit): Boolean {
        if (_dsgCurrency.value < 100) return false
        _dsgCurrency.value -= 100
        _rubies.value = _dsgCurrency.value

        val prizes = listOf(
            "Neon Viper M4A1",
            "Crimson Drake AWP",
            "Luxe Shahi Deagle",
            "Angel Suit Outfit",
            "Tactical Camo",
            "Golden Elite Armor",
            "Bonus +500 DSG!"
        )
        val selectedPrize = prizes.random()

        if (selectedPrize == "Bonus +500 DSG!") {
            _dsgCurrency.value += 500
            _rubies.value = _dsgCurrency.value
        } else {
            if (selectedPrize.contains("M4A1") || selectedPrize.contains("AWP") || selectedPrize.contains("Deagle")) {
                val current = _unlockedWeaponSkins.value.toMutableList()
                if (!current.contains(selectedPrize)) {
                    current.add(selectedPrize)
                    _unlockedWeaponSkins.value = current
                }
                // Auto equip on spin win
                val wpType = when {
                    selectedPrize.contains("M4A1") -> "M4A1"
                    selectedPrize.contains("AWP") -> "AWP"
                    else -> "DEAGLE"
                }
                changeWeaponSkin(wpType, selectedPrize)
            } else {
                val current = _unlockedOutfits.value.toMutableList()
                if (!current.contains(selectedPrize)) {
                    current.add(selectedPrize)
                    _unlockedOutfits.value = current
                }
                _equippedOutfit.value = selectedPrize
            }
        }
        onPrizeGranted(selectedPrize)
        return true
    }

    // Trigger Alok Healing, Chrono Forcefield, Kelly Agility burst, or K's EP mastery skills!
    fun triggerCharacterSkill(): String {
        if (_skillCooldown.value > 0 || _isSkillActive.value) return ""
        _isSkillActive.value = true
        _skillCooldown.value = 35 // 35 seconds cooldown timer

        val resultMsg = when (_equippedActiveSkill.value) {
            "Drop the Beat" -> "🎵 DROP THE BEAT: HEALING & SPEED AURA ACTIVE!"
            "Time Turner" -> "🛡️ TIME TURNER: GEODESIC FORCEFIELD ONLINE!"
            "Deadly Velocity" -> "⚡ DEADLY VELOCITY: FIREARM DAMAGE & PRECISION BOOST ACTIVE!"
            "Master of All" -> {
                _playerEp.value = (_playerEp.value + 150).coerceAtMost(250) // K's EP recovery
                val maxLimitHp = getMaxHp()
                _playerHp.value = (_playerHp.value + 40).coerceAtMost(maxLimitHp) // K's instant health burst
                "💚 MASTER OF ALL: INSTANT +150 EP & +40 HP RESTORED!"
            }
            else -> "⭐ HERO ACTIVE POWER INJECTED!"
        }

        // Log this heroic skill execution to the match feed!
        val currentLogs = _battleLogFeed.value.toMutableList()
        currentLogs.add("🔥 ${_profileName.value}: ACTIVE ${_equippedActiveSkill.value.uppercase()} POWER!")
        _battleLogFeed.value = currentLogs

        viewModelScope.launch {
            // Skill remains active for 10 seconds of high-powered state representation
            delay(10000)
            _isSkillActive.value = false

            // Process cooldown duration countdown
            while (_skillCooldown.value > 0) {
                delay(1000)
                _skillCooldown.value -= 1
            }
        }
        return resultMsg
    }

    // Real-time server simulation of 50 survivors matchmaking & killing each other
    fun restartBattleRoyaleMatching() {
        _survivorsCount.value = 50
        _arenaKills.value = 0
        _battleLogFeed.value = listOf(
            "📍 DIRECTING TO LOBBY // PING: 24ms",
            "⚡ MATCHMAKING COMPLETED // 50 RIVAL BOT PLAYERS ONLINE",
            "✈️ DEPLOYING SQUAD GLIDER INTO ZONE"
        )

        viewModelScope.launch {
            delay(2000)
            val weaponLogs = listOf("M4A1", "AWP Sniper", "Desert Eagle", "MP40", "M1014", "Katana")
            val botNames = listOf("Alok_Fanboy", "SaddarSlayer", "PortRanger", "RawalpindiPro", "ChronoMaster", "KellyRun", "GarenaX", "SniperGeni", "MughalFighter", "KPK_Warrior", "SindhSoldier")

            while (_survivorsCount.value > 1 && _currentScreen.value == ActiveScreen.ARENA) {
                delay(Random.nextLong(2200, 4800))
                if (_survivorsCount.value <= 1) break

                _survivorsCount.value -= 1

                val victim = botNames.random() + "_${Random.nextInt(10, 99)}"
                val killer = botNames.random() + "_${Random.nextInt(10, 99)}"
                if (victim != killer) {
                    val logs = _battleLogFeed.value.toMutableList()
                    logs.add("💀 $killer [${weaponLogs.random()}] $victim")
                    if (logs.size > 5) logs.removeAt(0)
                    _battleLogFeed.value = logs
                }
            }
        }
    }

    fun claimMailBonus(mailId: Int) {
        val updatedMail = _mailList.value.map { mail ->
            if (mail.id == mailId && !mail.isClaimed) {
                _dsgCurrency.value += mail.rubyBonus
                _rubies.value = _dsgCurrency.value
                mail.copy(isRead = true, isClaimed = true)
            } else mail
        }
        _mailList.value = updatedMail
    }

    fun playMail(mailId: Int) {
        val updatedMail = _mailList.value.map { mail ->
            if (mail.id == mailId) {
                mail.copy(isRead = true)
            } else mail
        }
        _mailList.value = updatedMail
    }

    fun sendChatMessage(text: String) {
        if (text.isEmpty()) return
        val list = _chatMessages.value.toMutableList()
        list.add(ChatMessage(_profileName.value, text, isUser = true))
        _chatMessages.value = list

        // Simulate immediate response
        viewModelScope.launch {
            delay(1500)
            val answers = listOf(
                "Nice, let's load up ${_selectedMap.value.label}!",
                "Ready up! I'm equipping my ${_selectedWeapon.value.label}.",
                "Wait, are we doing a match in ${_selectedWeather.value.label} conditions? Epic!",
                "Check custom camera angles, they look incredibly cool.",
                "Dead shot is so high-stakes!"
            )
            val autoResponses = _chatMessages.value.toMutableList()
            val sender = listOf("MargallaSniper", "KarachiKnight", "RaviWarrior").random()
            autoResponses.add(ChatMessage(sender, answers.random()))
            _chatMessages.value = autoResponses
        }
    }

    private fun simulateTeammateChats() {
        viewModelScope.launch {
            while (true) {
                delay(12000)
                if (_currentScreen.value == ActiveScreen.LOBBY) {
                    val messages = listOf(
                        ChatMessage("KarachiKnight", "Just set a personal best score of 1200 points in Karachi Fort!"),
                        ChatMessage("MargallaSniper", "AWP Sniper scope zoom is incredibly smooth with motion bobbing."),
                        ChatMessage("RaviWarrior", "Make sure you guys claim rewards in the Mail tab, rubies are free!"),
                        ChatMessage("KarachiKnight", "Who's launching sandstorm combat? It's really hard to aim!")
                    )
                    val oldList = _chatMessages.value.toMutableList()
                    oldList.add(messages.random())
                    if (oldList.size > 20) oldList.removeAt(0)
                    _chatMessages.value = oldList
                }
            }
        }
    }

    // GAMEPLAY MECHANICAL LOGIC
    fun setupArenaTargets() {
        val count = when (_selectedMap.value) {
            PakistanMap.KARACHI -> 5
            PakistanMap.LAHORE -> 6
            PakistanMap.ISLAMABAD -> 4
        }
        val targets = mutableListOf<TargetItem>()
        val rand = Random(42)
        for (i in 1..count) {
            val speedX = if (_selectedMap.value == PakistanMap.ISLAMABAD) rand.nextFloat() * 4f - 2f else 0f
            val maxHp = if (_selectedMap.value == PakistanMap.LAHORE) 150 else 100
            targets.add(
                TargetItem(
                    id = i,
                    x = 200f + rand.nextFloat() * 550f,
                    y = 120f + rand.nextFloat() * 180f,
                    size = if (i % 2 == 0) 65f else 50f,
                    hp = maxHp,
                    maxHp = maxHp,
                    speedX = speedX,
                    lastCrackSeed = Random.nextInt(100)
                )
            )
        }
        _scenicTargets.value = targets
        _floatingScores.value = emptyList()

        // Reset Free Fire battle parameters
        _playerHp.value = getMaxHp()
        _playerEp.value = 100
        _glooWallCount.value = 3
        _medkitCount.value = 3
        _isChannelingHeal.value = false
        _healProgress.value = 0f
        _isGlooWallDeployed.value = false
        _glooWallHp.value = 150
        _isBooyahActive.value = false
        _eliminationBadge.value = null
        _mocoTaggedTargetId.value = null
        _isMushroomVisible.value = true
        _mushroomX.value = 200f + Random.nextFloat() * 400f
        _mushroomY.value = 180f + Random.nextFloat() * 80f
        _isAirDropInbound.value = true
        _isAirDropLanded.value = false
        _isAirDropLooted.value = false
        _airDropY.value = -80f
        _airDropX.value = 200f + Random.nextFloat() * 400f

        restartBattleRoyaleMatching()

        // GARENA FREE FIRE DYNAMIC WEATHER MANAGER LOOP (Randomly rotates climate conditions every 15s)
        viewModelScope.launch {
            delay(12000) // Initial safe period in match
            while (_currentScreen.value == ActiveScreen.ARENA && !_isBooyahActive.value) {
                delay(15000)
                if (_currentScreen.value != ActiveScreen.ARENA || _isBooyahActive.value) break

                val otherWeathers = WeatherType.values().filter { it != _selectedWeather.value }
                if (otherWeathers.isNotEmpty()) {
                    val nextWeather = otherWeathers.random()
                    _selectedWeather.value = nextWeather
                    
                    val logs = _battleLogFeed.value.toMutableList()
                    val msg = when (nextWeather) {
                        WeatherType.SUNNY -> "☀️ CLIMATE SHIFTED: Sunny Weather conditions restored! High precision movement & aim stability active!"
                        WeatherType.MONSOON -> "🌧️ CLIMATE SHIFTED: Monsoon Rain strikes! Heavy precipitation reduces movement sensitivity by -30%!"
                        WeatherType.SANDSTORM -> "🌪️ CLIMATE SHIFTED: Dust Sandstorm active! Heavy wind gusts trigger real-time aim pointer drift!"
                    }
                    logs.add(msg)
                    if (logs.size > 5) logs.removeAt(0)
                    _battleLogFeed.value = logs
                }
            }
        }

        // Launch constant active healing aura and bot sniper alerts loop
        viewModelScope.launch {
            while (_currentScreen.value == ActiveScreen.ARENA && !_isBooyahActive.value) {
                delay(1000)
                // EP to HP passive healing if damaged
                val maxLimitHp = getMaxHp()
                if (_playerHp.value < maxLimitHp && _playerEp.value > 0) {
                    val healAmt = 4
                    _playerEp.value = (_playerEp.value - healAmt).coerceAtLeast(0)
                    _playerHp.value = (_playerHp.value + healAmt).coerceAtMost(maxLimitHp)
                }

                // DJ Alok "Drop the Beat" specialized dynamic fast healing aura (+25 HP / sec)
                if (_isSkillActive.value && _equippedActiveSkill.value == "Drop the Beat") {
                    _playerHp.value = (_playerHp.value + 25).coerceAtMost(getMaxHp())
                }
            }
        }

        // Air drop fall loop animation
        viewModelScope.launch {
            while (_currentScreen.value == ActiveScreen.ARENA && !_isBooyahActive.value) {
                delay(120)
                if (_isAirDropInbound.value && !_isAirDropLanded.value) {
                    val nextY = _airDropY.value + 5f
                    if (nextY >= 230f) {
                        _airDropY.value = 230f
                        _isAirDropLanded.value = true
                        val logs = _battleLogFeed.value.toMutableList()
                        logs.add("📢 AIRDROP ARRIVED COMET DEPOSIT IN ACCENT RADIUS!")
                        _battleLogFeed.value = logs
                    } else {
                        _airDropY.value = nextY
                    }
                }
            }
        }

        // Bot target sniper attacking back simulation
        viewModelScope.launch {
            delay(5000) // Initial safe period
            while (_currentScreen.value == ActiveScreen.ARENA && !_isBooyahActive.value) {
                // Bots shoot at us every 6 to 9 seconds
                delay(Random.nextLong(6000, 9500))
                if (_currentScreen.value != ActiveScreen.ARENA || _isBooyahActive.value) break

                val assailant = listOf("SniperElite_99", "GarenaPro_12", "ZoneBreaker", "LobbyKing").random()
                damagePlayer(25) { alertMsg ->
                    val logs = _battleLogFeed.value.toMutableList()
                    logs.add("💥 $assailant FIRED AT YOU!")
                    logs.add("   $alertMsg")
                    if (logs.size > 5) logs.removeAt(0)
                    _battleLogFeed.value = logs
                }
            }
        }
    }

    fun fireWeapon(xImpact: Float, yImpact: Float, onHitRegistered: (String) -> Unit) {
        if (_isReloading.value) return
        if (_currentAmmo.value <= 0) {
            reloadWeapon()
            return
        }

        _isFiring.value = true
        _currentAmmo.value -= 1

        // Trigger Weapon recoil kick animation
        viewModelScope.launch {
            _recoilOffset.value = when (_selectedWeapon.value) {
                WeaponType.DEAGLE -> -35f
                WeaponType.M4A1 -> -12f
                WeaponType.AWP -> -60f
            }
            delay(50)
            _isFiring.value = false
            // Gun settle animation back to rest
            var settle = _recoilOffset.value
            while (settle < 0f) {
                settle += 4f
                if (settle > 0f) settle = 0f
                _recoilOffset.value = settle
                delay(15)
            }
        }

        // Check hitting Free Fire auxiliary mushrooms/airdrops
        val auxMsg = checkAuxiliaryShots(xImpact, yImpact)
        if (auxMsg.isNotEmpty()) {
            onHitRegistered(auxMsg)
            if (_currentAmmo.value == 0) {
                reloadWeapon()
            }
            return
        }

        // Apply Weather wind/drift factors (Chrono shield isolates climate winds completely!)
        val windDriftX = if (_equippedActiveSkill.value == "Time Turner" && _isSkillActive.value) {
            0f
        } else {
            when (_selectedWeather.value) {
                WeatherType.SUNNY -> 0f
                WeatherType.MONSOON -> Random.nextFloat() * 10f - 5f
                WeatherType.SANDSTORM -> -18f // Steady sandstorm wind drifting left
            }
        }
        val adjustedX = xImpact + windDriftX
        val adjustedY = yImpact

        // Scan shot hit registers within target circles
        val list = _scenicTargets.value.toMutableList()
        var hitAny = false

        for (i in list.indices) {
            val target = list[i]
            if (target.isDestroyed) continue

            // Determine check distance relative to center
            val centerX = target.x + target.size / 2
            val centerY = target.y + target.size / 2
            val radius = target.size / 2

            val distSqr = (adjustedX - centerX) * (adjustedX - centerX) + (adjustedY - centerY) * (adjustedY - centerY)
            val inDistance = distSqr <= radius * radius

            if (inDistance) {
                hitAny = true
                val damageMultiplier = if (distSqr <= (radius / 3) * (radius / 3)) 2 else 1 // Headshot!
                
                // GARENA FREE FIRE KELLY DEADLY VELOCITY BOOST (Deals 2x damage when Deadly Velocity active skill is triggered!)
                val kellyDmgMultiplier = if (_equippedActiveSkill.value == "Deadly Velocity" && _isSkillActive.value) 2 else 1
                val hitDamage = _selectedWeapon.value.damage * damageMultiplier * kellyDmgMultiplier

                val newHp = (target.hp - hitDamage).coerceAtLeast(0)
                val isNowDestroyed = newHp <= 0

                list[i] = target.copy(
                    hp = newHp,
                    isDestroyed = isNowDestroyed,
                    lastCrackSeed = Random.nextInt(100)
                )

                _score.value += hitDamage

                // Trigger Moco's Hacker Eye styling highlight tracking
                if (_equippedPassiveSkill.value == "Moco's Hacker Eye") {
                    _mocoTaggedTargetId.value = target.id
                    viewModelScope.launch {
                        delay(4000)
                        if (_mocoTaggedTargetId.value == target.id) {
                            _mocoTaggedTargetId.value = null
                        }
                    }
                }
                
                // Reward DSG currency upon registered hits
                val dsgGained = if (damageMultiplier == 2) 15 else 8
                addDsgCurrency(dsgGained)

                if (isNowDestroyed) {
                    _arenaKills.value += 1
                    val bonusDsg = if (damageMultiplier == 2) 100 else 50
                    addDsgCurrency(bonusDsg)

                    // GARENA FREE FIRE ELIMINATION AUDIO-VISUAL POPUP BADGE LIFECYCLE
                    val currentKills = _arenaKills.value
                    viewModelScope.launch {
                        if (damageMultiplier == 2) {
                            _eliminationBadge.value = "HEADSHOT"
                        } else {
                            _eliminationBadge.value = when (currentKills) {
                                1 -> "FIRST BLOOD"
                                2 -> "DOUBLE KILL"
                                3 -> "TRIPLE KILL"
                                4 -> "QUADRA KILL"
                                else -> "ACE ELIMINATION"
                            }
                        }
                        delay(1250)
                        _eliminationBadge.value = null
                    }

                    // Inject instant user kill event into live battle feed
                    val victimBot = listOf("Bot_Noob", "DroneTarget", "SaddarSlayer", "KarachiGlider", "RookieShoot", "MughalSoldier").random() + "_${Random.nextInt(10, 99)}"
                    val logs = _battleLogFeed.value.toMutableList()
                    val wpShortName = _selectedWeapon.value.label.substringBefore(" ")
                    logs.add("⚡ HERO_${_profileName.value.uppercase()} [$wpShortName] $victimBot")
                    if (logs.size > 5) logs.removeAt(0)
                    _battleLogFeed.value = logs

                    onHitRegistered(if (damageMultiplier == 2) "🔥 BOID HEADSHOT ELIMINATION! +$bonusDsg DSG" else "🎯 TARGET COMPLETELY ELIMINATED! +$bonusDsg DSG")
                } else {
                    onHitRegistered(if (damageMultiplier == 2) "💥 CRITICAL HEADSHOT HIT! +$dsgGained DSG" else "🔫 BODYSURFACE HIT! +$dsgGained DSG")
                }
                break
            }
        }

        if (!hitAny) {
            onHitRegistered("MISSED!")
        }

        _scenicTargets.value = list

        if (list.isNotEmpty() && list.all { it.isDestroyed }) {
            forceBooyah()
            onHitRegistered("🎉 CHEES! BATTLE ROYALE CHAMPIONSHIP VICTORY! +1000 DSG")
        }

        // Check weapon auto-reload trigger
        if (_currentAmmo.value == 0) {
            reloadWeapon()
        }
    }

    fun reloadWeapon() {
        if (_isReloading.value) return
        _isReloading.value = true
        _isScoped.value = false // force unscoping on sniper reload

        viewModelScope.launch {
            // Simulate realistic tactical magazine swaps
            delay(1600)
            _currentAmmo.value = _selectedWeapon.value.ammoMax
            _isReloading.value = false
        }
    }

    fun updateMovingTargets(widthConstraint: Float) {
        val list = _scenicTargets.value.map { target ->
            if (target.isDestroyed || target.speedX == 0f) target
            else {
                var newX = target.x + target.speedX
                var nextSpeed = target.speedX
                // Boundary bounces inside width boundaries
                if (newX < 50f) {
                     newX = 50f
                     nextSpeed = -target.speedX
                } else if (newX > (widthConstraint - target.size - 50f)) {
                     newX = widthConstraint - target.size - 50f
                     nextSpeed = -target.speedX
                }
                target.copy(x = newX, speedX = nextSpeed)
            }
        }
        _scenicTargets.value = list
    }

    // GAME CONTROLS (Solo/Duo/Squad, Store, Vault, Settings, and Parachute Glider)
    fun selectGameMode(mode: String) {
        _selectedGameMode.value = mode
    }

    fun equipOutfit(outfitName: String) {
        if (_unlockedOutfits.value.contains(outfitName)) {
            _equippedOutfit.value = outfitName
        }
    }

    fun purchaseAngelBundle(cost: Int): Boolean {
        if (_rubies.value >= cost && !_isDailyLimitAngelBundlePurchased.value) {
            _rubies.value -= cost
            _isDailyLimitAngelBundlePurchased.value = true
            val unlocked = _unlockedOutfits.value.toMutableList()
            if (!unlocked.contains("Angel Suit Outfit")) {
                unlocked.add("Angel Suit Outfit")
            }
            _unlockedOutfits.value = unlocked
            _equippedOutfit.value = "Angel Suit Outfit"
            return true
        }
        return false
    }

    fun claimMidnightBonus() {
        _rubies.value += 120
    }

    fun setSensitivity(value: Float) {
        _sensitivityVal.value = value.coerceIn(0.1f, 3.0f)
    }

    fun setRedDotSize(value: Float) {
        _redDotSizeVal.value = value.coerceIn(4f, 30f)
    }

    fun setJoystickScale(value: Float) {
        _joystickScaleVal.value = value.coerceIn(0.5f, 2.00f)
    }

    fun setFireButtonScale(value: Float) {
        _fireButtonScaleVal.value = value.coerceIn(0.5f, 2.00f)
    }

    fun toggleRetroSoundOn() {
        _retroSoundOn.value = !_retroSoundOn.value
    }

    fun startParachuteGlideSequence(onFinished: () -> Unit) {
        _isParachuteGlidePhase.value = true
        _isParachuteOpened.value = false
        _parachuteDropHeight.value = 200f
        
        viewModelScope.launch {
            while (_parachuteDropHeight.value > 0f) {
                delay(120)
                val currentH = _parachuteDropHeight.value
                val step = if (currentH > 50f) 10f else 4f
                val newH = (currentH - step).coerceAtLeast(0f)
                _parachuteDropHeight.value = newH
                
                if (newH <= 50f) {
                    _isParachuteOpened.value = true
                }
            }
            _isParachuteGlidePhase.value = false
            _isParachuteOpened.value = false
            onFinished()
        }
    }

    fun skipParachuteGlide(onFinished: () -> Unit) {
        _isParachuteGlidePhase.value = false
        _isParachuteOpened.value = false
        _parachuteDropHeight.value = 0f
        onFinished()
    }
}
