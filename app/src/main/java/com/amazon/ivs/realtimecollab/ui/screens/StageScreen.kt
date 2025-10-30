package com.amazon.ivs.realtimecollab.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.amazon.ivs.realtimecollab.R
import com.amazon.ivs.realtimecollab.core.common.getMockParticipants
import com.amazon.ivs.realtimecollab.core.handlers.Destination
import com.amazon.ivs.realtimecollab.core.handlers.StageHandler
import com.amazon.ivs.realtimecollab.core.handlers.StageType
import com.amazon.ivs.realtimecollab.core.handlers.stage.Participant
import com.amazon.ivs.realtimecollab.core.handlers.stage.reorderScreenShareFirst
import com.amazon.ivs.realtimecollab.core.handlers.stage.reorderSelfFirst
import com.amazon.ivs.realtimecollab.core.handlers.stage.reorderSelfLast
import com.amazon.ivs.realtimecollab.ui.components.FadeBox
import com.amazon.ivs.realtimecollab.ui.components.KeepScreenOn
import com.amazon.ivs.realtimecollab.ui.components.OtherParticipants
import com.amazon.ivs.realtimecollab.ui.components.ParticipantView
import com.amazon.ivs.realtimecollab.ui.components.PreviewSurface
import com.amazon.ivs.realtimecollab.ui.components.ScreenColumn
import com.amazon.ivs.realtimecollab.ui.components.ScreenType
import com.amazon.ivs.realtimecollab.ui.components.StageControls
import com.amazon.ivs.realtimecollab.ui.components.StageIdBar
import com.amazon.ivs.realtimecollab.ui.components.StageMenu
import com.amazon.ivs.realtimecollab.ui.components.StageScreenPreview
import com.amazon.ivs.realtimecollab.ui.components.fillMaxPortraitSize
import com.amazon.ivs.realtimecollab.ui.components.fillMaxPortraitWidth
import com.amazon.ivs.realtimecollab.ui.components.getScreenType
import com.amazon.ivs.realtimecollab.ui.components.isLandscape
import com.amazon.ivs.realtimecollab.ui.components.isLessThanSquare
import com.amazon.ivs.realtimecollab.ui.components.isMultiWindow
import com.amazon.ivs.realtimecollab.ui.components.isPhone
import com.amazon.ivs.realtimecollab.ui.components.isPhoneLandscape
import com.amazon.ivs.realtimecollab.ui.components.isPortrait
import com.amazon.ivs.realtimecollab.ui.components.isSquareOrLandscape
import com.amazon.ivs.realtimecollab.ui.components.thenOptional
import com.amazon.ivs.realtimecollab.ui.theme.InterPrimary
import com.amazon.ivs.realtimecollab.ui.theme.WhitePrimary
import com.composeunstyled.Icon
import com.composeunstyled.Text
import timber.log.Timber
import kotlin.math.ceil

@Composable
fun StageScreen(
    destination: Destination,
) {
    val isVisible = destination == Destination.StageScreen
    val meetingConfig by StageHandler.meetingConfig.collectAsStateWithLifecycle()
    val stageType by StageHandler.stageType.collectAsStateWithLifecycle()
    val participants by StageHandler.participants.collectAsStateWithLifecycle()
    val stageId = meetingConfig.meetingId
    val publishers = participants.filter { !it.isViewer }

    FadeBox(
        isVisible = isVisible,
    ) {
        KeepScreenOn(keepScreenOn = publishers.isNotEmpty())
        StageScreenContent(
            stageId = stageId,
            stageType = stageType,
            participants = publishers,
        )
    }
}

@Composable
fun StageScreenContent(
    stageId: String,
    stageType: StageType,
    participants: List<Participant>,
    isMultiWindow: Boolean = isMultiWindow(),
    isLessThanSquare: Boolean = isLessThanSquare(),
    isLandscape: Boolean = isLandscape(),
    isPortrait: Boolean = isPortrait(),
    isPhoneLandscape: Boolean = isPhoneLandscape(),
    isSquareOrLandscape: Boolean = isSquareOrLandscape(),
    screenType: ScreenType = getScreenType(),
) {
    val isViewer = stageType == StageType.Viewer
    val isStageOnTheGo = stageType == StageType.OnTheGo
    val isStageOnTheGoLandscape = isStageOnTheGo && isLandscape

    ScreenColumn {
        StageHeader(
            isViewer = isViewer,
            stageId = stageId,
        )
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .thenOptional(enabled = !isStageOnTheGoLandscape) {
                    weight(1f)
                },
            contentAlignment = Alignment.Center,
        ) {
            FadeBox(
                isVisible = participants.isNotEmpty(),
            ) {
                if (isStageOnTheGo) {
                    if (isLandscape) return@FadeBox
                    StageOnTheGo(participants = participants)
                } else {
                    StageRegular(
                        participants = participants,
                        isPhoneLandscape = isPhoneLandscape,
                        isLessThanSquare = isLessThanSquare,
                        isPortrait = isPortrait,
                        isSquareOrLandscape = isSquareOrLandscape,
                        isMultiWindow = isMultiWindow,
                    )
                }
            }
            FadeBox(
                isVisible = participants.isEmpty(),
            ) {
                EmptyStage(
                    modifier = Modifier
                        .fillMaxPortraitWidth()
                        .fillMaxHeight(),
                )
            }
        }
        Box(
            modifier = Modifier
                .thenOptional(
                    enabled = isStageOnTheGoLandscape,
                    ifEnabled = {
                        fillMaxSize()
                    },
                    ifDisabled = {
                        fillMaxWidth()
                    }
                ),
            contentAlignment = if (isStageOnTheGoLandscape) Alignment.BottomCenter else Alignment.Center,
        ) {
            val showMenu = screenType.run { !isPhone() && isLandscape() }

            StageControls(
                stageType = stageType,
            )
            FadeBox(
                modifier = Modifier.align(
                    alignment = if (isStageOnTheGoLandscape) Alignment.BottomEnd else Alignment.CenterEnd,
                ),
                isVisible = showMenu
            ) {
                StageMenu()
            }
        }
    }
}

@Composable
private fun EmptyStage(
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(
            space = 10.dp,
            alignment = Alignment.CenterVertically
        ),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Icon(
            modifier = Modifier.size(30.dp),
            painter = painterResource(R.drawable.ic_dashed_circle),
            contentDescription = null,
            tint = WhitePrimary,
        )
        Text(
            modifier = Modifier.padding(horizontal = 70.dp),
            text = stringResource(R.string.no_contributors),
            style = InterPrimary,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun StageRegular(
    participants: List<Participant>,
    isLessThanSquare: Boolean,
    isMultiWindow: Boolean,
    isPhoneLandscape: Boolean,
    isPortrait: Boolean,
    isSquareOrLandscape: Boolean,
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        if (isPhoneLandscape || isMultiWindow) {
            ParticipantGrid(
                participants = participants,
                isMultiWindow = isMultiWindow,
                isLessThanSquare = isLessThanSquare,
                isPhoneLandscape = isPhoneLandscape,
                isPortrait = isPortrait,
                isSquareOrLandscape = isSquareOrLandscape,
            )
        } else {
            if (participants.size <= 3) {
                ParticipantColumn(participants = participants)
            } else {
                ParticipantGrid(
                    participants = participants,
                    isMultiWindow = false,
                    isPhoneLandscape = false,
                    isPortrait = isPortrait,
                    isSquareOrLandscape = isSquareOrLandscape,
                    isLessThanSquare = isLessThanSquare,
                )
            }
        }
    }
}

@Composable
private fun ParticipantColumn(
    participants: List<Participant>,
) {
    Box(
        modifier = Modifier.fillMaxHeight(),
    ) {
        Column(
            modifier = Modifier
                .fillMaxPortraitSize(maxWidth = 1024.dp),
            verticalArrangement = Arrangement.spacedBy(
                space = 10.dp,
                alignment = Alignment.CenterVertically
            ),
        ) {
            val visibleParticipant = participants.reorderSelfLast().reorderScreenShareFirst()
            Timber.d("Showing participant: ${visibleParticipant.map { it.id }}")
            visibleParticipant.forEach { participant ->
                ParticipantView(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    participant = participant,
                    setAspectRatio = false,
                )
            }
        }
    }
}

@Composable
private fun ParticipantGrid(
    participants: List<Participant>,
    isLessThanSquare: Boolean,
    isMultiWindow: Boolean,
    isPhoneLandscape: Boolean,
    isPortrait: Boolean,
    isSquareOrLandscape: Boolean,
) {
    val allParticipants = participants.reorderScreenShareFirst()
    val screenShareParticipant = allParticipants.firstOrNull { it.isScreenSharing }
    val filteredParticipants = allParticipants.filter { it.id != screenShareParticipant?.id }
    val isScreenSharing = screenShareParticipant != null
    val totalParticipantCount = filteredParticipants.size
    val isSmallMultiWindow = isLessThanSquare && isMultiWindow
    val maxVisibleParticipants = when {
        isSmallMultiWindow -> 2
        isScreenSharing && isMultiWindow -> 2
        isScreenSharing || isPhoneLandscape || isMultiWindow -> 4
        else -> 6
    }
    val visibleParticipantsCount = when {
        totalParticipantCount <= maxVisibleParticipants -> totalParticipantCount
        else -> maxVisibleParticipants - 1
    }
    val cellCount = totalParticipantCount.coerceAtMost(maximumValue = maxVisibleParticipants)
    val columnCount = when {
        isMultiWindow -> 2
        isPhoneLandscape -> 4
        isPortrait -> 2
        isSquareOrLandscape -> if (isScreenSharing) 4 else 3
        allParticipants.size > 4 -> 3
        else -> 2
    }
    val rowCount = ceil(cellCount.toDouble() / columnCount.toDouble()).toInt()
    val extraParticipantCount = totalParticipantCount - visibleParticipantsCount
    val showContent = (!isPhoneLandscape || !isScreenSharing) && !isSmallMultiWindow
    val visibleParticipants = filteredParticipants
        .reorderSelfFirst()
        .take(visibleParticipantsCount)
        .reorderSelfLast()
        .run {
            if (extraParticipantCount > 0) {
                if (visibleParticipantsCount == 1) {
                    val items: MutableList<Participant?> = toMutableList()
                    items.add(0, null)
                    items
                } else {
                    val count = (visibleParticipantsCount - 1).coerceAtLeast(0)
                    val items: MutableList<Participant?> = take(n = count).toMutableList()
                    if (count > 1) {
                        items.add(null)
                        items.add(lastOrNull())
                    }
                    items
                }
            } else {
                this
            }
        }
    val otherParticipants = filteredParticipants.subtract(visibleParticipants.mapNotNull { it }).toList()
    var participantIndex = 0
    Timber.d("Showing participant: ${visibleParticipants.map { it?.id ?: "null" }}")

    Column(
        modifier = Modifier
            .fillMaxPortraitSize(maxWidth = 1024.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        if (isScreenSharing) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                ParticipantView(
                    modifier = Modifier
                        .fillMaxHeight()
                        .weight(1f),
                    participant = screenShareParticipant,
                    setAspectRatio = false,
                    fillMaxWidth = false,
                )
                if (isSmallMultiWindow) {
                    if (filteredParticipants.size == 1) {
                        ParticipantView(
                            modifier = Modifier
                                .fillMaxHeight()
                                .weight(1f),
                            participant = filteredParticipants.first(),
                            setAspectRatio = false,
                            fillMaxWidth = false,
                            showContent = showContent,
                        )
                    } else {
                        OtherParticipants(
                            modifier = Modifier
                                .fillMaxHeight()
                                .weight(1f),
                            participants = filteredParticipants,
                            showContent = showContent,
                        )
                    }
                    return@Column
                }
            }
        }
        (0 until rowCount).forEach { _ ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .thenOptional(enabled = showContent || isSmallMultiWindow) {
                        if (isSquareOrLandscape && isScreenSharing && !isPhoneLandscape && !isMultiWindow) {
                            height(320.dp)
                        } else {
                            weight(1f)
                        }
                    },
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                (0 until columnCount).forEach { _ ->
                    val participant = visibleParticipants.getOrNull(participantIndex)
                    val modifier = Modifier
                        .thenOptional(
                            enabled = showContent || isSmallMultiWindow,
                            ifEnabled = {
                                fillMaxHeight()
                            },
                            ifDisabled = {
                                height(64.dp)
                            }
                        )
                        .weight(1f)
                    participantIndex++
                    if (participant == null && extraParticipantCount == 0) return

                    if (participant == null && otherParticipants.isNotEmpty()) {
                        OtherParticipants(
                            modifier = modifier,
                            participants = otherParticipants,
                            showContent = showContent,
                        )
                    }
                    if (participant != null) {
                        ParticipantView(
                            modifier = modifier,
                            participant = participant,
                            setAspectRatio = false,
                            fillMaxWidth = false,
                            showContent = showContent,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun StageOnTheGo(
    participants: List<Participant>,
) {
    val participant = participants.find { it.isSpeaker } ?: participants.firstOrNull() ?: return

    ParticipantView(
        participant = participant,
        avatarSize = 100.dp,
        textSize = 24.sp,
        showBackground = false,
    )
}

@Composable
private fun StageHeader(
    isViewer: Boolean,
    stageId: String,
) {
    val showMenu = getScreenType().run { isPhone() || isPortrait() }

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = if (showMenu) Arrangement.Start else Arrangement.Center,
    ) {
        StageIdBar(
            modifier = Modifier
                .thenOptional(showMenu) {
                    weight(1f)
                },
            stageId = stageId,
            isViewer = isViewer,
        )

        if (showMenu) {
            StageMenu()
        }
    }
}

@StageScreenPreview
@Composable
private fun StageScreenPortraitRegularPreview() {
    StageScreenPreview(
        stageType = StageType.Regular,
        participants = getMockParticipants(
            count = 0,
            screenShareIndexes = listOf(1, 4),
            cameraOnIndexes = listOf(1),
        ),
    )
}

@StageScreenPreview
@Composable
private fun StageScreenPortraitOnTheGoPreview() {
    StageScreenPreview(
        stageType = StageType.OnTheGo,
        participants = getMockParticipants(
            count = 16,
            screenShareIndexes = listOf(1, 4)
        ),
    )
}

@Composable
private fun StageScreenPreview(
    stageType: StageType,
    participants: List<Participant> = getMockParticipants(),
) {
    PreviewSurface {
        StageScreenContent(
            stageType = stageType,
            stageId = "otiw-osrl-7xgb",
            participants = participants,
        )
    }
}
