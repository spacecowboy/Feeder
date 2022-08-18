package com.nononsenseapps.feeder.ui.compose.sync

import android.app.Activity.RESULT_OK
import android.content.ActivityNotFoundException
import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.AlertDialog
import androidx.compose.material.Button
import androidx.compose.material.DropdownMenu
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.material.TextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.google.accompanist.insets.ui.Scaffold
import com.google.accompanist.insets.ui.TopAppBar
import com.nononsenseapps.feeder.BuildConfig
import com.nononsenseapps.feeder.R
import com.nononsenseapps.feeder.crypto.AesCbcWithIntegrity
import com.nononsenseapps.feeder.db.room.ID_UNSET
import com.nononsenseapps.feeder.db.room.SyncDevice
import com.nononsenseapps.feeder.ui.compose.minimumTouchSize
import com.nononsenseapps.feeder.ui.compose.theme.FeederTheme
import com.nononsenseapps.feeder.ui.compose.theme.LinkTextStyle
import com.nononsenseapps.feeder.ui.compose.theme.LocalDimens
import com.nononsenseapps.feeder.ui.compose.utils.BackHandler
import com.nononsenseapps.feeder.ui.compose.utils.ImmutableHolder
import com.nononsenseapps.feeder.ui.compose.utils.WindowSize
import com.nononsenseapps.feeder.util.DEEP_LINK_BASE_URI
import com.nononsenseapps.feeder.util.KOFI_URL
import com.nononsenseapps.feeder.util.openKoFiIntent
import java.net.URL
import java.net.URLDecoder
import net.glxn.qrgen.android.QRCode
import net.glxn.qrgen.core.scheme.Url

@Composable
private fun SyncScaffold(
    onNavigateUp: () -> Unit,
    onLeaveSyncChain: () -> Unit,
    content: @Composable (Modifier) -> Unit
) {
    var showToolbar by rememberSaveable {
        mutableStateOf(false)
    }
    Scaffold(
        contentPadding = WindowInsets.navigationBars.only(WindowInsetsSides.Horizontal).asPaddingValues(),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(id = R.string.device_sync),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                contentPadding = WindowInsets.systemBars.only(WindowInsetsSides.Horizontal + WindowInsetsSides.Top).asPaddingValues(),
                navigationIcon = {
                    IconButton(onClick = onNavigateUp) {
                        Icon(
                            Icons.Default.ArrowBack,
                            contentDescription = stringResource(R.string.go_back),
                        )
                    }
                },
                actions = {
                    Box {
                        IconButton(onClick = { showToolbar = true }) {
                            Icon(
                                Icons.Default.MoreVert,
                                contentDescription = stringResource(id = R.string.open_menu),
                            )
                        }
                        DropdownMenu(
                            expanded = showToolbar,
                            onDismissRequest = { showToolbar = false }
                        ) {
                            DropdownMenuItem(
                                onClick = {
                                    showToolbar = false
                                    onLeaveSyncChain()
                                }
                            ) {
                                Text(stringResource(R.string.leave_sync_chain))
                            }
                        }
                    }
                }
            )
        },
        content = {
            content(
                Modifier
                    .padding(it)
                    .navigationBarsPadding()
            )
        },
    )
}

@Composable
fun SyncScreen(
    windowSize: WindowSize,
    onNavigateUp: () -> Unit,
    viewModel: SyncScreenViewModel,
) {
    val viewState: SyncScreenViewState by viewModel.viewState.collectAsState()

    val syncScreenType = getSyncScreenType(
        windowSize = windowSize,
        viewState = viewState,
    )

    val launcher =
        rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                result.data?.getStringExtra("SCAN_RESULT")?.let {
                    val code = it.syncCodeQueryParam
                    if (code.isNotBlank()) {
                        viewModel.setSyncCode(code)
                    }
                    val secretKey = it.secretKeyQueryParam
                    if (secretKey.isNotBlank()) {
                        viewModel.setSecretKey(secretKey)
                    }
                }
            }
        }

    var showLeaveSyncChainDialog by rememberSaveable {
        mutableStateOf(false)
    }

    SyncScreen(
        viewState = viewState,
        syncScreenType = syncScreenType,
        onLeaveSyncSettings = onNavigateUp,
        onLeaveAddDevice = {
            viewModel.setScreen(SyncScreenToShow.DEVICELIST)
        },
        onLeaveSyncJoin = {
            viewModel.setScreen(SyncScreenToShow.SETUP)
        },
        onJoinSyncChain = { syncCode, secretKey ->
            viewModel.joinSyncChain(syncCode = syncCode, secretKey = secretKey)
        },
        onAddNewDevice = {
            viewModel.setScreen(SyncScreenToShow.ADD_DEVICE)
        },
        onDeleteDevice = {
            if (it == viewState.deviceId) {
                viewModel.leaveSyncChain()
            } else {
                viewModel.removeDevice(it)
            }
        },
        onLeaveSyncChain = {
            showLeaveSyncChainDialog = true
        },
        onScanSyncCode = {
            viewModel.setScreen(SyncScreenToShow.JOIN)
            // Open barcode scanner on open in case initialSyncCode is empty
            if (viewState.syncCode.isEmpty()) {
                try {
                    launcher.launch(Intent("com.google.zxing.client.android.SCAN"))
                } catch (e: ActivityNotFoundException) {
                    viewModel.onMissingBarCodeScanner()
                }
            }
        },
        onStartNewSyncChain = {
            viewModel.startNewSyncChain()
        },
        onSetSyncCode = {
            viewModel.setSyncCode(it)
        },
        onSetSecretKey = {
            viewModel.setSecretKey(it)
        },
        currentDeviceId = viewState.deviceId,
        devices = ImmutableHolder(viewState.deviceList),
    )

    if (showLeaveSyncChainDialog) {
        LeaveSyncChainDialog(
            onDismiss = {
                showLeaveSyncChainDialog = false
            },
            onOk = {
                showLeaveSyncChainDialog = false
                viewModel.leaveSyncChain()
            },
        )
    }
}

@Composable
fun SyncScreen(
    viewState: SyncScreenViewState,
    syncScreenType: SyncScreenType,
    onLeaveSyncSettings: () -> Unit,
    onLeaveAddDevice: () -> Unit,
    onLeaveSyncJoin: () -> Unit,
    onJoinSyncChain: (String, String) -> Unit,
    onAddNewDevice: () -> Unit,
    onDeleteDevice: (Long) -> Unit,
    onScanSyncCode: () -> Unit,
    onStartNewSyncChain: () -> Unit,
    onSetSyncCode: (String) -> Unit,
    onSetSecretKey: (String) -> Unit,
    currentDeviceId: Long,
    devices: ImmutableHolder<List<SyncDevice>>,
    onLeaveSyncChain: () -> Unit,
) {
    when (syncScreenType) {
        SyncScreenType.DUAL -> {
            DualSyncScreen(
                onNavigateUp = onLeaveSyncSettings,
                leftScreenToShow = viewState.leftScreenToShow,
                rightScreenToShow = viewState.rightScreenToShow,
                onScanSyncCode = onScanSyncCode,
                onStartNewSyncChain = onStartNewSyncChain,
                onAddNewDevice = onAddNewDevice,
                onDeleteDevice = onDeleteDevice,
                currentDeviceId = currentDeviceId,
                devices = devices,
                addDeviceUrl = ImmutableHolder(viewState.addNewDeviceUrl),
                onJoinSyncChain = onJoinSyncChain,
                syncCode = viewState.syncCode,
                onSetSyncCode = onSetSyncCode,
                onLeaveSyncChain = onLeaveSyncChain,
                secretKey = viewState.secretKey,
                onSetSecretKey = onSetSecretKey,
            )
        }
        SyncScreenType.SINGLE_SETUP -> {
            SyncSetupScreen(
                onNavigateUp = onLeaveSyncSettings,
                onScanSyncCode = onScanSyncCode,
                onStartNewSyncChain = onStartNewSyncChain,
                onLeaveSyncChain = onLeaveSyncChain,
            )
        }
        SyncScreenType.SINGLE_DEVICELIST -> {
            SyncDeviceListScreen(
                onNavigateUp = onLeaveSyncSettings,
                currentDeviceId = currentDeviceId,
                devices = devices,
                onAddNewDevice = onAddNewDevice,
                onDeleteDevice = onDeleteDevice,
                onLeaveSyncChain = onLeaveSyncChain,
            )
        }
        SyncScreenType.SINGLE_ADD_DEVICE -> {
            SyncAddNewDeviceScreen(
                onNavigateUp = onLeaveAddDevice,
                syncUrl = ImmutableHolder(viewState.addNewDeviceUrl),
                onLeaveSyncChain = onLeaveSyncChain,
            )
        }
        SyncScreenType.SINGLE_JOIN -> {
            SyncJoinScreen(
                onNavigateUp = onLeaveSyncJoin,
                onJoinSyncChain = onJoinSyncChain,
                syncCode = viewState.syncCode,
                onSetSyncCode = onSetSyncCode,
                onLeaveSyncChain = onLeaveSyncChain,
                secretKey = viewState.secretKey,
                onSetSecretKey = onSetSecretKey,
            )
        }
    }
}

enum class SyncScreenType {
    DUAL,
    SINGLE_SETUP,
    SINGLE_DEVICELIST,
    SINGLE_ADD_DEVICE,
    SINGLE_JOIN,
}

fun getSyncScreenType(
    windowSize: WindowSize,
    viewState: SyncScreenViewState
): SyncScreenType = when (windowSize) {
    WindowSize.Compact, WindowSize.Medium -> {
        when (viewState.singleScreenToShow) {
            SyncScreenToShow.SETUP -> SyncScreenType.SINGLE_SETUP
            SyncScreenToShow.DEVICELIST -> SyncScreenType.SINGLE_DEVICELIST
            SyncScreenToShow.ADD_DEVICE -> SyncScreenType.SINGLE_ADD_DEVICE
            SyncScreenToShow.JOIN -> SyncScreenType.SINGLE_JOIN
        }
    }
    WindowSize.Expanded -> SyncScreenType.DUAL
}

@Composable
fun DualSyncScreen(
    onNavigateUp: () -> Unit,
    leftScreenToShow: LeftScreenToShow,
    rightScreenToShow: RightScreenToShow,
    onScanSyncCode: () -> Unit,
    onStartNewSyncChain: () -> Unit,
    onAddNewDevice: () -> Unit,
    onDeleteDevice: (Long) -> Unit,
    currentDeviceId: Long,
    devices: ImmutableHolder<List<SyncDevice>>,
    addDeviceUrl: ImmutableHolder<URL>,
    onJoinSyncChain: (String, String) -> Unit,
    syncCode: String,
    onSetSyncCode: (String) -> Unit,
    secretKey: String,
    onSetSecretKey: (String) -> Unit,
    onLeaveSyncChain: () -> Unit,
) {
    BackHandler(onBack = onNavigateUp)

    SyncScaffold(
        onNavigateUp = onNavigateUp,
        onLeaveSyncChain = onLeaveSyncChain,
    ) { modifier ->
        Row(
            modifier = modifier,
        ) {
            when (leftScreenToShow) {
                LeftScreenToShow.SETUP -> {
                    SyncSetupContent(
                        onScanSyncCode = onScanSyncCode,
                        onStartNewSyncChain = onStartNewSyncChain,
                        modifier = Modifier
                            .weight(1f, fill = true)
                    )
                }
                LeftScreenToShow.DEVICELIST -> {
                    SyncDeviceListContent(
                        currentDeviceId = currentDeviceId,
                        devices = devices,
                        onAddNewDevice = onAddNewDevice,
                        onDeleteDevice = onDeleteDevice,
                        showAddDeviceButton = false,
                        modifier = Modifier
                            .weight(1f, fill = true)
                    )
                }
            }

            when (rightScreenToShow) {
                RightScreenToShow.ADD_DEVICE -> {
                    SyncAddNewDeviceContent(
                        syncUrl = addDeviceUrl,
                        modifier = Modifier
                            .weight(1f, fill = true)
                    )
                }
                RightScreenToShow.JOIN -> {
                    SyncJoinContent(
                        modifier = Modifier
                            .weight(1f, fill = true),
                        onJoinSyncChain = onJoinSyncChain,
                        syncCode = syncCode,
                        onSetSyncCode = onSetSyncCode,
                        secretKey = secretKey,
                        onSetSecretKey = onSetSecretKey,
                    )
                }
            }
        }
    }
}

@Composable
fun SyncSetupScreen(
    onNavigateUp: () -> Unit,
    onScanSyncCode: () -> Unit,
    onStartNewSyncChain: () -> Unit,
    onLeaveSyncChain: () -> Unit,
) {
    BackHandler(onBack = onNavigateUp)

    SyncScaffold(
        onNavigateUp = onNavigateUp,
        onLeaveSyncChain = onLeaveSyncChain,
    ) { modifier ->
        SyncSetupContent(
            modifier = modifier,
            onScanSyncCode = onScanSyncCode,
            onStartNewSyncChain = onStartNewSyncChain,
        )
    }
}

@Composable
fun SyncSetupContent(
    modifier: Modifier,
    onScanSyncCode: () -> Unit,
    onStartNewSyncChain: () -> Unit,
) {
    val dimens = LocalDimens.current

    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = dimens.margin, vertical = 8.dp)
    ) {
        Text(
            text = stringResource(R.string.device_sync_description_1),
            style = MaterialTheme.typography.body1,
            modifier = Modifier.fillMaxWidth(),
        )
        Text(
            text = stringResource(R.string.device_sync_description_2),
            style = MaterialTheme.typography.body1,
            modifier = Modifier.fillMaxWidth(),
        )
        Text(
            text = stringResource(R.string.device_sync_financed_by_community),
            style = MaterialTheme.typography.body1,
            modifier = Modifier.fillMaxWidth(),
        )
        // Google Play does not allow direct donation links
        if (!BuildConfig.BUILD_TYPE.contains("play", ignoreCase = true)) {
            val context = LocalContext.current
            Text(
                text = KOFI_URL,
                style = MaterialTheme.typography.body1.merge(LinkTextStyle()),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        context.startActivity(openKoFiIntent())
                    },
            )
        }
        // Let this be hard-coded. It should not be localized.
        Text(
            text = "WARNING! This is a Beta feature. Do an OPML-export of all your feeds before and save as a backup.",
            style = MaterialTheme.typography.subtitle1,
            modifier = Modifier.fillMaxWidth(),
        )

        Spacer(modifier = Modifier.size(24.dp))

        Button(
            onClick = onScanSyncCode
        ) {
            Text(
                text = stringResource(R.string.scan_or_enter_code),
                style = MaterialTheme.typography.button,
            )
        }
        TextButton(
            onClick = onStartNewSyncChain
        ) {
            Text(
                text = stringResource(R.string.start_new_sync_chain),
                style = MaterialTheme.typography.button,
            )
        }
    }
}

internal val String.syncCodeQueryParam
    get() = substringAfter("sync_code=").take(64)

internal val String.secretKeyQueryParam
    get() = substringAfter("key=")
        .substringBefore("&")
        .let {
            // Deeplinks are already decoded - but not if you scan a QR code
            if ("%3A" in it) {
                URLDecoder.decode(it, "UTF-8")
            } else {
                it
            }
        }

@Composable
fun SyncJoinScreen(
    onNavigateUp: () -> Unit,
    onJoinSyncChain: (String, String) -> Unit,
    syncCode: String,
    onSetSyncCode: (String) -> Unit,
    secretKey: String,
    onSetSecretKey: (String) -> Unit,
    onLeaveSyncChain: () -> Unit,
) {
    BackHandler(onBack = onNavigateUp)

    SyncScaffold(
        onNavigateUp = onNavigateUp,
        onLeaveSyncChain = onLeaveSyncChain,
    ) { modifier ->
        SyncJoinContent(
            modifier = modifier,
            onJoinSyncChain = onJoinSyncChain,
            syncCode = syncCode,
            onSetSyncCode = onSetSyncCode,
            secretKey = secretKey,
            onSetSecretKey = onSetSecretKey,
        )
    }
}

@Composable
fun SyncJoinContent(
    modifier: Modifier,
    onJoinSyncChain: (String, String) -> Unit,
    syncCode: String,
    onSetSyncCode: (String) -> Unit,
    secretKey: String,
    onSetSecretKey: (String) -> Unit,
) {
    val dimens = LocalDimens.current

    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = dimens.margin, vertical = 8.dp)
    ) {
        TextField(
            value = syncCode,
            label = {
                Text(text = stringResource(R.string.sync_code))
            },
            onValueChange = onSetSyncCode,
            isError = syncCode.syncCodeQueryParam.length != 64,
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 64.dp)
        )
        TextField(
            value = secretKey,
            label = {
                Text(text = stringResource(R.string.secret_key))
            },
            onValueChange = onSetSecretKey,
            isError = !AesCbcWithIntegrity.isKeyDecodable(secretKey),
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 64.dp)
        )
        Button(
            enabled = syncCode.syncCodeQueryParam.length == 64,
            onClick = {
                onJoinSyncChain(syncCode, secretKey)
            }
        ) {
            Text(
                text = stringResource(R.string.join_sync_chain),
                style = MaterialTheme.typography.button,
            )
        }
    }
}

@Composable
fun SyncDeviceListScreen(
    onNavigateUp: () -> Unit,
    currentDeviceId: Long,
    devices: ImmutableHolder<List<SyncDevice>>,
    onAddNewDevice: () -> Unit,
    onDeleteDevice: (Long) -> Unit,
    onLeaveSyncChain: () -> Unit,
) {
    BackHandler(onBack = onNavigateUp)

    SyncScaffold(
        onNavigateUp = onNavigateUp,
        onLeaveSyncChain = onLeaveSyncChain,
    ) { modifier ->
        SyncDeviceListContent(
            modifier = modifier,
            currentDeviceId = currentDeviceId,
            devices = devices,
            onAddNewDevice = onAddNewDevice,
            onDeleteDevice = onDeleteDevice,
            showAddDeviceButton = true,
        )
    }
}

@Composable
fun SyncDeviceListContent(
    modifier: Modifier,
    currentDeviceId: Long,
    devices: ImmutableHolder<List<SyncDevice>>,
    onAddNewDevice: () -> Unit,
    onDeleteDevice: (Long) -> Unit,
    showAddDeviceButton: Boolean,
) {
    var itemToDelete by remember {
        mutableStateOf(SyncDevice(deviceId = ID_UNSET, deviceName = ""))
    }

    val dimens = LocalDimens.current

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = dimens.margin, vertical = 8.dp)
    ) {
        Text(
            text = stringResource(R.string.devices_on_sync_chain),
            style = MaterialTheme.typography.subtitle1,
            modifier = Modifier
                .padding(top = 8.dp, bottom = 8.dp)
                .fillMaxWidth()
        )
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f, fill = true)
        ) {
            items(
                count = devices.item.count(),
                key = { devices.item[it].deviceId }
            ) { index ->
                val device = devices.item[index]

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = minimumTouchSize)
                ) {
                    val text = if (device.deviceId == currentDeviceId) {
                        stringResource(id = R.string.this_device, device.deviceName)
                    } else {
                        device.deviceName
                    }
                    Text(
                        text = text,
                        style = MaterialTheme.typography.subtitle1,
                        modifier = Modifier.weight(1f, fill = true)
                    )
                    IconButton(
                        onClick = {
                            itemToDelete = device
                        },
                    ) {
                        Icon(
                            Icons.Filled.Delete,
                            contentDescription = stringResource(R.string.disconnect_device_from_sync),
                        )
                    }
                }
            }
        }
        Text(
            text = stringResource(R.string.device_sync_financed_by_community),
            style = MaterialTheme.typography.body1,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
        )
        // Google Play does not allow direct donation links
        if (!BuildConfig.BUILD_TYPE.contains("play", ignoreCase = true)) {
            val context = LocalContext.current
            Text(
                text = KOFI_URL,
                style = MaterialTheme.typography.body1.merge(LinkTextStyle()),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        context.startActivity(openKoFiIntent())
                    },
            )
        }
        if (showAddDeviceButton) {
            Button(
                onClick = onAddNewDevice,
                modifier = Modifier.padding(top = 8.dp)
            ) {
                Text(
                    text = stringResource(R.string.add_new_device),
                    style = MaterialTheme.typography.button,
                    textAlign = TextAlign.Center,
                )
            }
        }
        Spacer(modifier = Modifier.size(8.dp))
    }

    if (itemToDelete.deviceId != ID_UNSET) {
        DeleteDeviceDialog(
            deviceName = itemToDelete.deviceName,
            onDismiss = { itemToDelete = SyncDevice(deviceId = ID_UNSET, deviceName = "") }
        ) {
            onDeleteDevice(itemToDelete.deviceId)
            itemToDelete = SyncDevice(deviceId = ID_UNSET, deviceName = "")
        }
    }
}

@Composable
fun DeleteDeviceDialog(
    deviceName: String,
    onDismiss: () -> Unit,
    onOk: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            Button(onClick = onOk) {
                Text(text = stringResource(id = android.R.string.ok))
            }
        },
        dismissButton = {
            Button(onClick = onDismiss) {
                Text(text = stringResource(id = android.R.string.cancel))
            }
        },
        title = {
            Text(
                text = stringResource(R.string.remove_device),
                style = MaterialTheme.typography.h6,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .padding(vertical = 8.dp)
            )
        },
        text = {
            Text(
                text = stringResource(R.string.remove_device_question, deviceName),
                style = MaterialTheme.typography.body1,
            )
        }
    )
}

@Composable
fun SyncAddNewDeviceScreen(
    onNavigateUp: () -> Unit,
    syncUrl: ImmutableHolder<URL>,
    onLeaveSyncChain: () -> Unit,
) {
    BackHandler(onBack = onNavigateUp)

    SyncScaffold(
        onNavigateUp = onNavigateUp,
        onLeaveSyncChain = onLeaveSyncChain,
    ) { modifier ->
        SyncAddNewDeviceContent(
            modifier = modifier,
            syncUrl = syncUrl,
        )
    }
}

@Composable
fun SyncAddNewDeviceContent(
    modifier: Modifier,
    syncUrl: ImmutableHolder<URL>,
) {
    val context = LocalContext.current

    val qrCode by remember(syncUrl) {
        derivedStateOf {
            QRCode.from(
                Url().also {
                    it.url = "${syncUrl.item}"
                }
            )
                .withSize(1000, 1000)
                .bitmap()
                .asImageBitmap()
        }
    }
    val dimens = LocalDimens.current

    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = dimens.margin, vertical = 8.dp)
            .scrollable(
                state = rememberScrollState(),
                orientation = Orientation.Vertical,
            )
    ) {
        Text(
            text = stringResource(R.string.press_scan_sync),
            style = MaterialTheme.typography.body1,
            modifier = Modifier
                .fillMaxWidth(),
        )
        Text(
            text = stringResource(R.string.or_open_device_sync_link),
            style = MaterialTheme.typography.body1,
            modifier = Modifier
                .fillMaxWidth(),
        )
        Text(
            text = stringResource(R.string.treat_like_password),
            style = MaterialTheme.typography.body1.copy(color = Color.Red),
            modifier = Modifier
                .fillMaxWidth(),
        )
        Image(
            bitmap = qrCode,
            contentDescription = stringResource(R.string.qr_code),
            contentScale = ContentScale.Fit,
            modifier = Modifier.size(250.dp)
        )
        val intentTitle = stringResource(R.string.feeder_device_sync_code)
        Text(
            text = "$syncUrl",
            style = LinkTextStyle(),
            modifier = Modifier
                .fillMaxWidth()
                .clickable {
                    val intent = Intent.createChooser(
                        Intent(Intent.ACTION_SEND).apply {
                            putExtra(Intent.EXTRA_TEXT, "$syncUrl")
                            putExtra(Intent.EXTRA_TITLE, intentTitle)
                            type = "text/plain"
                        },
                        null
                    )
                    context.startActivity(intent)
                }
        )
    }
}

@Preview("Device List Tablet", device = Devices.PIXEL_C)
@Composable
fun PreviewDualSyncScreenDeviceList() {
    FeederTheme {
        DualSyncScreen(
            onNavigateUp = { },
            leftScreenToShow = LeftScreenToShow.DEVICELIST,
            rightScreenToShow = RightScreenToShow.ADD_DEVICE,
            onScanSyncCode = { },
            onStartNewSyncChain = { },
            onAddNewDevice = { },
            onDeleteDevice = {},
            onLeaveSyncChain = {},
            currentDeviceId = 5L,
            devices = ImmutableHolder(
                listOf(
                    SyncDevice(deviceId = 1L, deviceName = "ONEPLUS A6003"),
                    SyncDevice(deviceId = 2L, deviceName = "SM-T970"),
                    SyncDevice(deviceId = 3L, deviceName = "Nexus 6"),
                )
            ),
            addDeviceUrl = ImmutableHolder(URL("$DEEP_LINK_BASE_URI/sync/join?sync_code=123foo")),
            onJoinSyncChain = { _, _ -> },
            syncCode = "",
            onSetSyncCode = {},
            secretKey = "",
            onSetSecretKey = {},
        )
    }
}

@Preview("Setup Tablet", device = Devices.PIXEL_C)
@Composable
fun PreviewDualSyncScreenSetup() {
    FeederTheme {
        DualSyncScreen(
            onNavigateUp = { },
            leftScreenToShow = LeftScreenToShow.SETUP,
            rightScreenToShow = RightScreenToShow.JOIN,
            onScanSyncCode = { },
            onStartNewSyncChain = { },
            onAddNewDevice = { },
            onDeleteDevice = {},
            onLeaveSyncChain = {},
            currentDeviceId = 5L,
            devices = ImmutableHolder(
                listOf(
                    SyncDevice(deviceId = 1L, deviceName = "ONEPLUS A6003"),
                    SyncDevice(deviceId = 2L, deviceName = "SM-T970"),
                    SyncDevice(deviceId = 3L, deviceName = "Nexus 6"),
                )
            ),
            addDeviceUrl = ImmutableHolder(URL("$DEEP_LINK_BASE_URI/sync/join?sync_code=123foo&key=123ABF")),
            onJoinSyncChain = { _, _ -> },
            syncCode = "",
            onSetSyncCode = {},
            secretKey = "",
            onSetSecretKey = {},
        )
    }
}

@Preview("Scan or Enter Phone")
@Preview("Scan or Enter Small Tablet", device = Devices.NEXUS_7_2013)
@Composable
fun PreviewJoin() {
    FeederTheme {
        SyncJoinScreen(
            onNavigateUp = {},
            onJoinSyncChain = { _, _ -> },
            syncCode = "",
            onSetSyncCode = {},
            onLeaveSyncChain = {},
            secretKey = "",
            onSetSecretKey = {},
        )
    }
}

@Preview("Empty Phone")
@Preview("Empty Small Tablet", device = Devices.NEXUS_7_2013)
@Composable
fun PreviewEmpty() {
    FeederTheme {
        SyncSetupScreen(
            onNavigateUp = {},
            onScanSyncCode = {},
            onStartNewSyncChain = {},
            onLeaveSyncChain = {},
        )
    }
}

@Preview("Device List Phone")
@Preview("Device List Small Tablet", device = Devices.NEXUS_7_2013)
@Composable
fun PreviewDeviceList() {
    FeederTheme {
        SyncDeviceListScreen(
            onNavigateUp = {},
            currentDeviceId = 5L,
            devices = ImmutableHolder(
                listOf(
                    SyncDevice(deviceId = 1L, deviceName = "ONEPLUS A6003"),
                    SyncDevice(deviceId = 2L, deviceName = "SM-T970"),
                    SyncDevice(deviceId = 3L, deviceName = "Nexus 6"),
                )
            ),
            onAddNewDevice = {},
            onDeleteDevice = {},
            onLeaveSyncChain = {},
        )
    }
}

@Preview("Add New Device Phone")
@Preview("Add New Device Small Tablet", device = Devices.NEXUS_7_2013)
@Composable
fun PreviewAddNewDeviceContent() {
    FeederTheme {
        SyncAddNewDeviceScreen(
            onNavigateUp = {},
            onLeaveSyncChain = {},
            syncUrl = ImmutableHolder(URL("https://feeder-sync.nononsenseapps.com/join?sync_code=1234abc572335asdbc&key=123ABF")),
        )
    }
}
