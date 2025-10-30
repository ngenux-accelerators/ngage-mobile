package com.amazon.ivs.realtimecollab.ui.bottomsheets

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.amazon.ivs.realtimecollab.R
import com.amazon.ivs.realtimecollab.core.common.getMockParticipants
import com.amazon.ivs.realtimecollab.core.handlers.StageHandler
import com.amazon.ivs.realtimecollab.core.handlers.stage.Participant
import com.amazon.ivs.realtimecollab.ui.components.BoringAvatar
import com.amazon.ivs.realtimecollab.ui.components.FadeBox
import com.amazon.ivs.realtimecollab.ui.components.MultiPreview
import com.amazon.ivs.realtimecollab.ui.components.PreviewSurface
import com.amazon.ivs.realtimecollab.ui.components.isDesktopLandscape
import com.amazon.ivs.realtimecollab.ui.components.isTabletPortrait
import com.amazon.ivs.realtimecollab.ui.components.screenHeight
import com.amazon.ivs.realtimecollab.ui.components.thenOptional
import com.amazon.ivs.realtimecollab.ui.theme.InterHint
import com.amazon.ivs.realtimecollab.ui.theme.InterTitle
import com.amazon.ivs.realtimecollab.ui.theme.WhitePrimary

@Composable
fun MembersBottomSheet() {
    val members by StageHandler.members.collectAsStateWithLifecycle()

    MembersBottomSheetContent(
        members = members,
    )
}

@Composable
private fun MembersBottomSheetContent(
    members: List<Participant>,
) {
    val maxHeight = screenHeight() / 2

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .thenOptional(
                enabled = isTabletPortrait(),
                ifEnabled = {
                    height(maxHeight)
                },
                ifDisabled = {
                    fillMaxHeight()
                }
            )
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(space = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        val publishers = members.filter { !it.isViewer }
        val viewers = members.filter { it.isViewer }

        FadeBox(
            isVisible = members.isNotEmpty()
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(space = 20.dp),
            ) {
                if (publishers.isNotEmpty()) {
                    item {
                        Text(
                            modifier = Modifier.fillMaxWidth(),
                            text = stringResource(R.string.on_this_stage),
                            style = InterTitle,
                            textAlign = TextAlign.Center,
                        )
                    }
                    items(
                        items = publishers,
                        key = { it.id },
                    ) { member ->
                        MembersItem(
                            member = member,
                        )
                    }
                }
                if (viewers.isNotEmpty()) {
                    item {
                        Text(
                            modifier = Modifier.fillMaxWidth().padding(top = 10.dp),
                            text = stringResource(R.string.viewers),
                            style = InterTitle,
                            textAlign = TextAlign.Center,
                        )
                    }
                    items(
                        items = viewers,
                        key = { it.id },
                    ) { member ->
                        MembersItem(
                            member = member,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun MembersItem(
    member: Participant
) {
    Row(
        modifier = Modifier.padding(start = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        BoringAvatar(
            name = member.name,
            avatarSize = 38.dp,
        )
        Text(
            text = member.name,
            style = InterHint.copy(color = WhitePrimary),
        )
    }
}

@MultiPreview
@Composable
private fun MembersBottomSheetLoaded() {
    MembersBottomSheetPreview()
}

@Composable
private fun MembersBottomSheetPreview(
    members: List<Participant> = getMockParticipants(
        count = 8,
        viewerIndexes = listOf(3, 5),
    ),
) {
    PreviewSurface {
        BottomSheetContainer(
            contentAlignment = if (isDesktopLandscape()) Alignment.BottomEnd else Alignment.BottomCenter,
        ) {
            MembersBottomSheetContent(
                members = members,
            )
        }
    }
}
