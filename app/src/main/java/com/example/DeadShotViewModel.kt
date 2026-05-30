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
    private val _rubies = MutableStateFlow(1250)
    val rubies: StateFlow<Int> = _rubies.asStateFlow()

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
        _rubies.value += amount
    }

    fun claimMailBonus(mailId: Int) {
        val updatedMail = _mailList.value.map { mail ->
            if (mail.id == mailId && !mail.isClaimed) {
                _rubies.value += mail.rubyBonus
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

        // Apply Weather wind/drift factors
        val windDriftX = when (_selectedWeather.value) {
            WeatherType.SUNNY -> 0f
            WeatherType.MONSOON -> Random.nextFloat() * 10f - 5f
            WeatherType.SANDSTORM -> -18f // Steady sandstorm wind drifting left
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
                val hitDamage = _selectedWeapon.value.damage * damageMultiplier

                val newHp = (target.hp - hitDamage).coerceAtLeast(0)
                val isNowDestroyed = newHp <= 0

                list[i] = target.copy(
                    hp = newHp,
                    isDestroyed = isNowDestroyed,
                    lastCrackSeed = Random.nextInt(100)
                )

                _score.value += hitDamage
                addRubies(hitDamage / 5)

                if (isNowDestroyed) {
                    onHitRegistered(if (damageMultiplier == 2) "CRITICAL DESTRUCTION! +$hitDamage" else "TARGET DESTRUCTED! +$hitDamage")
                } else {
                    onHitRegistered(if (damageMultiplier == 2) "HEADSHOT HIT! -$hitDamage" else "HIT! -$hitDamage")
                }
                break
            }
        }

        if (!hitAny) {
            onHitRegistered("MISSED!")
        }

        _scenicTargets.value = list

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
}
