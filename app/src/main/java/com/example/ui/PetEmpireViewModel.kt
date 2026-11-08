package com.example.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.CalculationEngine
import com.example.data.EmpireFinancialSummary
import com.example.data.ExpansionPlanEvaluation
import com.example.data.GlobalSettingsEntity
import com.example.data.HouseComputedStatus
import com.example.data.PetEmpireRepository
import com.example.data.PetTier
import com.example.util.AlarmActionItem
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class HouseFilter {
    ALL,
    WARNINGS_ONLY,
    RUNNING,
    PAUSED
}

data class Tuple4<A, B, C, D>(val a: A, val b: B, val c: C, val d: D)

data class PetEmpireUiState(
    val isLoading: Boolean = true,
    val settings: GlobalSettingsEntity = GlobalSettingsEntity(),
    val activeHouses: List<HouseComputedStatus> = emptyList(),
    val filteredHouses: List<HouseComputedStatus> = emptyList(),
    val selectedFilter: HouseFilter = HouseFilter.ALL,
    val searchQuery: String = "",
    val selectedHouseDetail: HouseComputedStatus? = null,
    val summary: EmpireFinancialSummary = CalculationEngine.computeEmpireSummary(
        GlobalSettingsEntity(),
        emptyList()
    ),
    val expansionEvaluation: ExpansionPlanEvaluation = CalculationEngine.evaluateExpansionPlan(
        GlobalSettingsEntity()
    ),
    val lastAlarmMessage: String? = null,
    val pendingChangedAlarmsCount: Int = 0
)

class PetEmpireViewModel(
    private val repository: PetEmpireRepository
) : ViewModel() {

    private val filterFlow = MutableStateFlow(HouseFilter.ALL)
    private val searchQueryFlow = MutableStateFlow("")
    private val selectedHouseNumberFlow = MutableStateFlow<Int?>(null)
    private val nowTickerFlow = MutableStateFlow(System.currentTimeMillis())
    private val lastAlarmMessageFlow = MutableStateFlow<String?>(null)
    private val changedTimerKeysFlow = MutableStateFlow<Set<String>>(emptySet())

    init {
        viewModelScope.launch {
            repository.ensureSeeded(System.currentTimeMillis())
        }
        // Active countdown timer loop: recomputes elapsed and remaining times every 1000ms
        viewModelScope.launch {
            while (true) {
                delay(1000L)
                nowTickerFlow.value = System.currentTimeMillis()
            }
        }
    }

    val uiState: StateFlow<PetEmpireUiState> = combine(
        repository.globalSettingsFlow,
        repository.housesFlow,
        filterFlow,
        searchQueryFlow,
        combine(selectedHouseNumberFlow, nowTickerFlow, lastAlarmMessageFlow, changedTimerKeysFlow) { sel, now, alarm, changedKeys ->
            Tuple4(sel, now, alarm, changedKeys)
        }
    ) { settings, houses, filter, query, tuple ->
        val selectedHouseNum = tuple.a
        val nowMs = tuple.b
        val alarmMsg = tuple.c
        val changedKeys = tuple.d
        val computedAll = houses.map { house ->
            CalculationEngine.computeHouseStatus(house, settings, nowMs)
        }
        val activeHouses = computedAll.take(settings.targetHouseCount.coerceIn(1, 50))
        val filtered = activeHouses.filter { h ->
            val matchesFilter = when (filter) {
                HouseFilter.ALL -> true
                HouseFilter.WARNINGS_ONLY -> h.hasOverflowWarning || h.hasExpiryWarning
                HouseFilter.RUNNING -> h.activeRunningSlotsCount > 0
                HouseFilter.PAUSED -> h.activeRunningSlotsCount == 0
            }
            val matchesQuery = query.isBlank() ||
                h.entity.customLabel.contains(query, ignoreCase = true) ||
                h.entity.houseNumber.toString() == query.trim()
            matchesFilter && matchesQuery
        }

        val summary = CalculationEngine.computeEmpireSummary(settings, computedAll)
        val expansionEval = CalculationEngine.evaluateExpansionPlan(settings)
        val detailHouse = selectedHouseNum?.let { num ->
            computedAll.firstOrNull { it.entity.houseNumber == num }
        }

        PetEmpireUiState(
            isLoading = houses.isEmpty(),
            settings = settings,
            activeHouses = activeHouses,
            filteredHouses = filtered,
            selectedFilter = filter,
            searchQuery = query,
            selectedHouseDetail = detailHouse,
            summary = summary,
            expansionEvaluation = expansionEval,
            lastAlarmMessage = alarmMsg,
            pendingChangedAlarmsCount = changedKeys.size
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000L),
        initialValue = PetEmpireUiState()
    )

    fun setHouseFilter(filter: HouseFilter) {
        filterFlow.value = filter
    }

    fun setSearchQuery(query: String) {
        searchQueryFlow.value = query
    }

    fun selectHouseForDetail(houseNumber: Int?) {
        selectedHouseNumberFlow.value = houseNumber
    }

    fun notifyAlarmScheduled(message: String) {
        lastAlarmMessageFlow.value = message
    }

    fun clearAlarmMessage() {
        lastAlarmMessageFlow.value = null
    }

    // --- Global Settings Actions ---
    fun updateTargetHouseCount(count: Int) {
        viewModelScope.launch {
            repository.updateGlobalSettings { it.copy(targetHouseCount = count.coerceIn(1, 50)) }
        }
    }

    fun updateEconomySettings(
        targetHouseCount: Int,
        sTierSellPrice: Long,
        aTierSellPrice: Long,
        fCoinExchangeRatePer1000: Long,
        penyaToUsdPer200M: Double,
        houseDeedCost: Long,
        baseFPetCost: Long,
        macCostFCoins: Int,
        macMaxActiveHours: Double,
        accumulatedProfitsPenya: Long
    ) {
        viewModelScope.launch {
            repository.updateGlobalSettings {
                it.copy(
                    targetHouseCount = targetHouseCount.coerceIn(1, 50),
                    sTierSellPrice = sTierSellPrice.coerceAtLeast(0L),
                    aTierSellPrice = aTierSellPrice.coerceAtLeast(0L),
                    fCoinExchangeRatePer1000 = fCoinExchangeRatePer1000.coerceAtLeast(1000L),
                    penyaToUsdPer200M = penyaToUsdPer200M.coerceAtLeast(0.01),
                    houseDeedCost = houseDeedCost.coerceAtLeast(0L),
                    baseFPetCost = baseFPetCost.coerceAtLeast(0L),
                    macCostFCoins = macCostFCoins.coerceAtLeast(1),
                    macMaxActiveHours = macMaxActiveHours.coerceAtLeast(1.0),
                    accumulatedProfitsPenya = accumulatedProfitsPenya.coerceAtLeast(0L)
                )
            }
        }
    }

    fun updateAccumulatedProfits(newAmountPenya: Long) {
        viewModelScope.launch {
            repository.updateGlobalSettings {
                it.copy(accumulatedProfitsPenya = newAmountPenya.coerceAtLeast(0L))
            }
        }
    }

    fun addCompletedCycleProfitToTreasury() {
        val cycleProfit = uiState.value.summary.empireNetProfitCycle
        viewModelScope.launch {
            repository.updateGlobalSettings {
                it.copy(accumulatedProfitsPenya = (it.accumulatedProfitsPenya + cycleProfit).coerceAtLeast(0L))
            }
        }
    }

    fun resetGlobalSettings() {
        viewModelScope.launch {
            repository.resetGlobalSettingsToDefaults()
        }
    }

    // --- Expansion Planner & Harvest Day Actions ---
    fun updateExpansionPlannerSettings(
        currentHouses: Int,
        targetHouses: Int,
        availableSeeds: Int,
        currentCycleHour: Double
    ) {
        viewModelScope.launch {
            repository.updateGlobalSettings {
                it.copy(
                    currentOwnedHousesForPlanner = currentHouses.coerceIn(0, 50),
                    targetExpansionHouses = targetHouses.coerceIn(1, 50),
                    availableAPetSeeds = availableSeeds.coerceIn(0, 200),
                    currentCycleHour = currentCycleHour.coerceIn(0.0, 250.0)
                )
            }
        }
    }

    // --- Manual House & Slot Timer Controls ---
    fun toggleSlotRunning(houseNumber: Int, slotIndex: Int) {
        val nowMs = System.currentTimeMillis()
        nowTickerFlow.value = nowMs
        viewModelScope.launch {
            repository.updateHouse(houseNumber) { house ->
                when (slotIndex) {
                    1 -> {
                        if (house.slot1Running) {
                            val delta = house.slot1LastStartedEpochMs?.let {
                                ((nowMs - it).coerceAtLeast(0L)) / 3_600_000.0
                            } ?: 0.0
                            house.copy(
                                slot1Running = false,
                                slot1AccumulatedHours = house.slot1AccumulatedHours + delta,
                                slot1LastStartedEpochMs = null
                            )
                        } else {
                            house.copy(
                                slot1Running = true,
                                slot1LastStartedEpochMs = nowMs
                            )
                        }
                    }
                    2 -> {
                        if (house.slot2Running) {
                            val delta = house.slot2LastStartedEpochMs?.let {
                                ((nowMs - it).coerceAtLeast(0L)) / 3_600_000.0
                            } ?: 0.0
                            house.copy(
                                slot2Running = false,
                                slot2AccumulatedHours = house.slot2AccumulatedHours + delta,
                                slot2LastStartedEpochMs = null
                            )
                        } else {
                            house.copy(
                                slot2Running = true,
                                slot2LastStartedEpochMs = nowMs
                            )
                        }
                    }
                    3 -> {
                        if (house.slot3Running) {
                            val delta = house.slot3LastStartedEpochMs?.let {
                                ((nowMs - it).coerceAtLeast(0L)) / 3_600_000.0
                            } ?: 0.0
                            house.copy(
                                slot3Running = false,
                                slot3AccumulatedHours = house.slot3AccumulatedHours + delta,
                                slot3LastStartedEpochMs = null
                            )
                        } else {
                            house.copy(
                                slot3Running = true,
                                slot3LastStartedEpochMs = nowMs
                            )
                        }
                    }
                    else -> house
                }
            }
        }
    }

    fun resetSlotTimer(houseNumber: Int, slotIndex: Int) {
        val nowMs = System.currentTimeMillis()
        nowTickerFlow.value = nowMs
        viewModelScope.launch {
            repository.updateHouse(houseNumber) { house ->
                when (slotIndex) {
                    1 -> house.copy(
                        slot1Running = false,
                        slot1AccumulatedHours = 0.0,
                        slot1LastStartedEpochMs = null
                    )
                    2 -> house.copy(
                        slot2Running = false,
                        slot2AccumulatedHours = 0.0,
                        slot2LastStartedEpochMs = null
                    )
                    3 -> house.copy(
                        slot3Running = false,
                        slot3AccumulatedHours = 0.0,
                        slot3LastStartedEpochMs = null
                    )
                    else -> house
                }
            }
        }
    }

    /**
     * Direct exact adjustment of accumulated hours on a slot.
     */
    fun adjustSlotHours(houseNumber: Int, slotIndex: Int, newHours: Double, keepRunning: Boolean? = null) {
        val nowMs = System.currentTimeMillis()
        nowTickerFlow.value = nowMs
        val clamped = newHours.coerceAtLeast(0.0)
        markTimerChanged("h${houseNumber}_s$slotIndex")
        viewModelScope.launch {
            repository.updateHouse(houseNumber) { house ->
                when (slotIndex) {
                    1 -> {
                        val running = keepRunning ?: house.slot1Running
                        house.copy(
                            slot1Running = running,
                            slot1AccumulatedHours = clamped,
                            slot1LastStartedEpochMs = if (running) nowMs else null
                        )
                    }
                    2 -> {
                        val running = keepRunning ?: house.slot2Running
                        house.copy(
                            slot2Running = running,
                            slot2AccumulatedHours = clamped,
                            slot2LastStartedEpochMs = if (running) nowMs else null
                        )
                    }
                    3 -> {
                        val running = keepRunning ?: house.slot3Running
                        house.copy(
                            slot3Running = running,
                            slot3AccumulatedHours = clamped,
                            slot3LastStartedEpochMs = if (running) nowMs else null
                        )
                    }
                    else -> house
                }
            }
        }
    }

    /**
     * Sets exactly how many remaining hours are left until this pet finishes its rank/bake.
     * Computes elapsed = required - remaining.
     */
    fun setSlotRemainingHours(houseNumber: Int, slotIndex: Int, remainingHours: Double) {
        val nowMs = System.currentTimeMillis()
        nowTickerFlow.value = nowMs
        val remClamped = remainingHours.coerceAtLeast(0.0)
        markTimerChanged("h${houseNumber}_s$slotIndex")
        viewModelScope.launch {
            repository.updateHouse(houseNumber) { house ->
                when (slotIndex) {
                    1 -> {
                        val req = PetTier.S_TIER_BAKE_HOURS
                        val newAccumulated = (req - remClamped).coerceAtLeast(0.0)
                        house.copy(
                            slot1AccumulatedHours = newAccumulated,
                            slot1LastStartedEpochMs = if (house.slot1Running) nowMs else null
                        )
                    }
                    2 -> {
                        val tier = PetTier.fromCode(house.slot2Tier)
                        val req = if (tier == PetTier.A) 0.0 else tier.stageDurationHours
                        val newAccumulated = (req - remClamped).coerceAtLeast(0.0)
                        house.copy(
                            slot2AccumulatedHours = newAccumulated,
                            slot2LastStartedEpochMs = if (house.slot2Running) nowMs else null
                        )
                    }
                    3 -> {
                        val tier = PetTier.fromCode(house.slot3Tier)
                        val req = if (tier == PetTier.A) 0.0 else tier.stageDurationHours
                        val newAccumulated = (req - remClamped).coerceAtLeast(0.0)
                        house.copy(
                            slot3AccumulatedHours = newAccumulated,
                            slot3LastStartedEpochMs = if (house.slot3Running) nowMs else null
                        )
                    }
                    else -> house
                }
            }
        }
    }

    /**
     * Sets remaining hours left on an NPC replacement timer (against max 360h).
     * Computes hoursUsed = maxHours - remainingHours.
     */
    fun setMacRemainingHours(houseNumber: Int, grade: PetTier, remainingHours: Double) {
        val maxHours = uiState.value.settings.macMaxActiveHours
        val remClamped = remainingHours.coerceIn(0.0, maxHours)
        val used = (maxHours - remClamped).coerceAtLeast(0.0)
        markTimerChanged("h${houseNumber}_mac_${grade.code}")
        viewModelScope.launch {
            repository.updateHouse(houseNumber) { house ->
                when (grade) {
                    PetTier.F -> house.copy(macFHoursUsed = used)
                    PetTier.E -> house.copy(macEHoursUsed = used)
                    PetTier.D -> house.copy(macDHoursUsed = used)
                    PetTier.C -> house.copy(macCHoursUsed = used)
                    PetTier.B -> house.copy(macBHoursUsed = used)
                    PetTier.A -> house.copy(macAHoursUsed = used)
                }
            }
        }
    }

    fun setPipelineSlotTier(houseNumber: Int, slotIndex: Int, tier: PetTier) {
        val nowMs = System.currentTimeMillis()
        nowTickerFlow.value = nowMs
        markTimerChanged("h${houseNumber}_s$slotIndex")
        viewModelScope.launch {
            repository.updateHouse(houseNumber) { house ->
                when (slotIndex) {
                    2 -> house.copy(
                        slot2Tier = tier.code,
                        slot2AccumulatedHours = 0.0,
                        slot2LastStartedEpochMs = if (house.slot2Running) nowMs else null
                    )
                    3 -> house.copy(
                        slot3Tier = tier.code,
                        slot3AccumulatedHours = 0.0,
                        slot3LastStartedEpochMs = if (house.slot3Running) nowMs else null
                    )
                    else -> house
                }
            }
        }
    }

    fun renewMacGradeTimer(houseNumber: Int, grade: PetTier) {
        viewModelScope.launch {
            repository.updateHouse(houseNumber) { house ->
                when (grade) {
                    PetTier.F -> house.copy(macFHoursUsed = 0.0)
                    PetTier.E -> house.copy(macEHoursUsed = 0.0)
                    PetTier.D -> house.copy(macDHoursUsed = 0.0)
                    PetTier.C -> house.copy(macCHoursUsed = 0.0)
                    PetTier.B -> house.copy(macBHoursUsed = 0.0)
                    PetTier.A -> house.copy(macAHoursUsed = 0.0)
                }
            }
        }
    }

    fun renewAllMacTimersForHouse(houseNumber: Int) {
        viewModelScope.launch {
            repository.updateHouse(houseNumber) { house ->
                house.copy(
                    macFHoursUsed = 0.0,
                    macEHoursUsed = 0.0,
                    macDHoursUsed = 0.0,
                    macCHoursUsed = 0.0,
                    macBHoursUsed = 0.0,
                    macAHoursUsed = 0.0
                )
            }
        }
    }

    // --- Empire-Wide Bulk Setups ---

    /**
     * User workflow example: "moved all B pets to A, C to B, and added an F pet in each house".
     * Advances Slot 2 (B -> A), Slot 3 (C -> B or advances 1 tier), starts a fresh F tier, and resets elapsed to 0h.
     */
    fun applyEmpireBulkPromotionSetup() {
        val nowMs = System.currentTimeMillis()
        nowTickerFlow.value = nowMs
        viewModelScope.launch {
            repository.updateAllHouses { house ->
                // Move Slot 2 to Grade A (ready for bake/harvest)
                // Move Slot 3 (e.g. C) to Grade B
                // Start slot 2 with Grade A or fresh slot 3 with fresh F
                house.copy(
                    slot1Running = true,
                    slot1LastStartedEpochMs = nowMs,
                    slot2Tier = "A",
                    slot2Running = true,
                    slot2AccumulatedHours = 0.0,
                    slot2LastStartedEpochMs = nowMs,
                    slot3Tier = "B",
                    slot3Running = true,
                    slot3AccumulatedHours = 0.0,
                    slot3LastStartedEpochMs = nowMs
                )
            }
        }
    }

    fun markTimerChanged(key: String) {
        changedTimerKeysFlow.value = changedTimerKeysFlow.value + key
    }

    fun clearAllChangedTimers() {
        changedTimerKeysFlow.value = emptySet()
    }

    /**
     * Collects pending actions across active houses.
     * If [onlyChanged] is true, only returns actions for timers that were recently modified/set.
     */
    fun getAllEmpireAlarmActionItems(onlyChanged: Boolean = false): List<AlarmActionItem> {
        val state = uiState.value
        val changedKeys = changedTimerKeysFlow.value
        val items = mutableListOf<AlarmActionItem>()

        state.activeHouses.forEach { house ->
            val num = house.entity.houseNumber

            // Slot 1
            val s1Key = "h${num}_s1"
            if ((!onlyChanged || changedKeys.contains(s1Key)) && house.slot1.isRunning && house.slot1.remainingHours > 0.02) {
                items.add(
                    AlarmActionItem(
                        hoursFromNow = house.slot1.remainingHours,
                        label = "House #$num: Slot 1 S-Tier Bake Complete"
                    )
                )
            }

            // Slot 2
            val s2Key = "h${num}_s2"
            if ((!onlyChanged || changedKeys.contains(s2Key)) && house.slot2.isRunning && house.slot2.remainingHours > 0.02) {
                items.add(
                    AlarmActionItem(
                        hoursFromNow = house.slot2.remainingHours,
                        label = "House #$num: Slot 2 Tier ${house.slot2.tier.code} Complete"
                    )
                )
            }

            // Slot 3
            val s3Key = "h${num}_s3"
            if ((!onlyChanged || changedKeys.contains(s3Key)) && house.slot3.isRunning && house.slot3.remainingHours > 0.02) {
                items.add(
                    AlarmActionItem(
                        hoursFromNow = house.slot3.remainingHours,
                        label = "House #$num: Slot 3 Tier ${house.slot3.tier.code} Complete"
                    )
                )
            }

            // Mac NPC Expiry (if < 48 hours remaining)
            house.macTimers.filter { it.isExpiringSoon && it.hoursRemaining > 0.05 }.forEach { mac ->
                val macKey = "h${num}_mac_${mac.grade.code}"
                if (!onlyChanged || changedKeys.contains(macKey)) {
                    items.add(
                        AlarmActionItem(
                            hoursFromNow = mac.hoursRemaining,
                            label = "House #$num: Mac ${mac.grade.code} NPC Expired (360h)"
                        )
                    )
                }
            }
        }

        return items
    }

    fun pauseAllFinishedPets() {
        val nowMs = System.currentTimeMillis()
        nowTickerFlow.value = nowMs
        val settings = uiState.value.settings
        viewModelScope.launch {
            repository.updateAllHouses { house ->
                val status = CalculationEngine.computeHouseStatus(house, settings, nowMs)
                house.copy(
                    slot1Running = if (status.slot1.isFinished) false else house.slot1Running,
                    slot1AccumulatedHours = if (status.slot1.isFinished) {
                        status.slot1.requiredHours
                    } else {
                        status.slot1.elapsedHours
                    },
                    slot1LastStartedEpochMs = if (status.slot1.isFinished || !house.slot1Running) null else nowMs,
                    slot2Running = if (status.slot2.isFinished) false else house.slot2Running,
                    slot2AccumulatedHours = if (status.slot2.isFinished) {
                        status.slot2.requiredHours
                    } else {
                        status.slot2.elapsedHours
                    },
                    slot2LastStartedEpochMs = if (status.slot2.isFinished || !house.slot2Running) null else nowMs,
                    slot3Running = if (status.slot3.isFinished) false else house.slot3Running,
                    slot3AccumulatedHours = if (status.slot3.isFinished) {
                        status.slot3.requiredHours
                    } else {
                        status.slot3.elapsedHours
                    },
                    slot3LastStartedEpochMs = if (status.slot3.isFinished || !house.slot3Running) null else nowMs
                )
            }
        }
    }

    fun synchronizeAllHousesToHarvestDayHourZero() {
        val nowMs = System.currentTimeMillis()
        nowTickerFlow.value = nowMs
        viewModelScope.launch {
            repository.updateGlobalSettings {
                it.copy(currentCycleHour = 0.0)
            }
            repository.updateAllHouses { house ->
                house.copy(
                    isSeededWithAPet = true,
                    slot1Running = true,
                    slot1AccumulatedHours = 0.0,
                    slot1LastStartedEpochMs = nowMs,
                    slot2Tier = "F",
                    slot2Running = true,
                    slot2AccumulatedHours = 0.0,
                    slot2LastStartedEpochMs = nowMs,
                    slot3Tier = "F",
                    slot3Running = true,
                    slot3AccumulatedHours = 0.0,
                    slot3LastStartedEpochMs = nowMs
                )
            }
        }
    }

    fun setAllHousesRunningState(running: Boolean) {
        val nowMs = System.currentTimeMillis()
        nowTickerFlow.value = nowMs
        val settings = uiState.value.settings
        viewModelScope.launch {
            repository.updateAllHouses { house ->
                val status = CalculationEngine.computeHouseStatus(house, settings, nowMs)
                house.copy(
                    slot1Running = running,
                    slot1AccumulatedHours = status.slot1.elapsedHours,
                    slot1LastStartedEpochMs = if (running) nowMs else null,
                    slot2Running = running,
                    slot2AccumulatedHours = status.slot2.elapsedHours,
                    slot2LastStartedEpochMs = if (running) nowMs else null,
                    slot3Running = running,
                    slot3AccumulatedHours = status.slot3.elapsedHours,
                    slot3LastStartedEpochMs = if (running) nowMs else null
                )
            }
        }
    }

    class Factory(private val repository: PetEmpireRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return PetEmpireViewModel(repository) as T
        }
    }
}
