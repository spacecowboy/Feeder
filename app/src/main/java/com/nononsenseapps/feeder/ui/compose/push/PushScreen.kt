package com.nononsenseapps.feeder.ui.compose.push

import android.app.Activity.RESULT_OK
import android.content.ActivityNotFoundException
import android.content.Intent
import android.util.Base64
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.nononsenseapps.feeder.BuildConfig
import com.nononsenseapps.feeder.R
import com.nononsenseapps.feeder.db.room.KnownDevice
import com.nononsenseapps.feeder.db.room.ThisDevice
import com.nononsenseapps.feeder.push.Device
import com.nononsenseapps.feeder.push.Devices as ProtoDevices
import com.nononsenseapps.feeder.push.toProto
import com.nononsenseapps.feeder.ui.compose.minimumTouchSize
import com.nononsenseapps.feeder.ui.compose.settings.MenuSetting
import com.nononsenseapps.feeder.ui.compose.theme.DynamicTopAppBar
import com.nononsenseapps.feeder.ui.compose.theme.FeederTheme
import com.nononsenseapps.feeder.ui.compose.theme.LinkTextStyle
import com.nononsenseapps.feeder.ui.compose.theme.LocalDimens
import com.nononsenseapps.feeder.ui.compose.theme.SetStatusBarColorToMatchScrollableTopAppBar
import com.nononsenseapps.feeder.ui.compose.theme.dynamicScrollBehavior
import com.nononsenseapps.feeder.ui.compose.utils.ImmutableHolder
import com.nononsenseapps.feeder.ui.compose.utils.LocalWindowSize
import com.nononsenseapps.feeder.ui.compose.utils.ScreenType
import com.nononsenseapps.feeder.ui.compose.utils.WindowSize
import com.nononsenseapps.feeder.ui.compose.utils.getScreenType
import com.nononsenseapps.feeder.util.DEEP_LINK_BASE_URI
import com.nononsenseapps.feeder.util.KOFI_URL
import com.nononsenseapps.feeder.util.logDebug
import com.nononsenseapps.feeder.util.openKoFiIntent
import com.nononsenseapps.feeder.util.openUrlIntent
import java.net.URL
import java.net.URLDecoder
import net.glxn.qrgen.android.QRCode
import net.glxn.qrgen.core.scheme.Url
import org.threeten.bp.Instant

const val BASE64_QR_FLAGS = Base64.NO_WRAP and Base64.URL_SAFE

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PushScaffold(
    onNavigateUp: () -> Unit,
    leavePushVisible: Boolean,
    onLeaveSyncChain: () -> Unit,
    title: String,
    content: @Composable (Modifier) -> Unit
) {
    var showToolbar by rememberSaveable {
        mutableStateOf(false)
    }
    val scrollBehavior = dynamicScrollBehavior()

    SetStatusBarColorToMatchScrollableTopAppBar(scrollBehavior)

    Scaffold(
        modifier = Modifier
            .nestedScroll(scrollBehavior.nestedScrollConnection)
            .windowInsetsPadding(WindowInsets.navigationBars.only(WindowInsetsSides.Horizontal)),
        contentWindowInsets = WindowInsets.statusBars,
        topBar = {
            DynamicTopAppBar(
                scrollBehavior = scrollBehavior,
                title = title,
                navigationIcon = {
                    IconButton(onClick = onNavigateUp) {
                        Icon(
                            Icons.Default.ArrowBack,
                            contentDescription = stringResource(R.string.go_back),
                        )
                    }
                },
                actions = {
                    if (leavePushVisible) {
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
                                    },
                                    text = {
                                        Text(stringResource(R.string.leave_sync_chain))
                                    }
                                )
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
fun PushScreen(
    onNavigateUp: () -> Unit,
    viewModel: PushScreenViewModel,
) {
    val viewState: PushScreenViewState by viewModel.viewState.collectAsState()

    val windowSize = LocalWindowSize()
    val pushScreenType = getPushScreenType(
        windowSize = windowSize,
        viewState = viewState,
    )

    var previousScreen: PushScreenType? by remember {
        mutableStateOf(null)
    }

    val launcher =
        rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                result.data?.getStringExtra("SCAN_RESULT")?.let {
                    logDebug("JONAS", "result: $it")
                    // TODO error handling
                    val devices = ProtoDevices.ADAPTER.decode(
                        Base64.decode(it, BASE64_QR_FLAGS)
                    )
                    viewModel.joinSyncChain(devices)
                    // TODO barcode
//                    val code = it.pushCodeQueryParam
//                    if (code.isNotBlank()) {
//                        viewModel.setPushCode(code)
//                    }
//                    val secretKey = it.secretKeyQueryParam
//                    if (secretKey.isNotBlank()) {
//                        viewModel.setSecretKey(secretKey)
//                    }
                }
            }
        }

    var showLeaveSyncChainDialog by rememberSaveable {
        mutableStateOf(false)
    }

    PushScreen(
        viewState = viewState,
        targetScreen = pushScreenType,
        previousScreen = previousScreen,
        onLeavePushSettings = onNavigateUp,
        onLeaveAddDevice = {
            previousScreen = PushScreenType.SINGLE_ADD_DEVICE
            viewModel.setScreen(PushScreenToShow.DEVICELIST)
        },
        onLeavePushJoin = {
            previousScreen = PushScreenType.SINGLE_JOIN
            viewModel.setScreen(PushScreenToShow.SETUP)
        },
        onJoinSyncChain = { devices ->
            viewModel.joinSyncChain(devices = devices)
        },
        onAddNewDevice = {
            previousScreen = PushScreenType.SINGLE_DEVICELIST
            viewModel.setScreen(PushScreenToShow.ADD_DEVICE)
        },
        onDeleteDevice = {
            viewModel.deleteDevice(it)
        },
        onLeaveSyncChain = {
            previousScreen = PushScreenType.SINGLE_DEVICELIST
            showLeaveSyncChainDialog = true
        },
        onScanPushCode = {
//            viewModel.setScreen(PushScreenToShow.JOIN)
//             Open barcode scanner on open in case initialPushCode is empty
//             TODO
//            if (viewState.pushCode.isEmpty()) {
            try {
                launcher.launch(Intent("com.google.zxing.client.android.SCAN"))
            } catch (e: ActivityNotFoundException) {
                // TODO
//                    viewModel.onMissingBarCodeScanner()
            }
//            }
        },
        onSelectedDistributor = {
            viewModel.setDistributor(it)
        },
    )

    if (showLeaveSyncChainDialog) {
        // TODO
//        LeaveSyncChainDialog(
//            onDismiss = {
//                showLeaveSyncChainDialog = false
//            },
//            onOk = {
//                showLeaveSyncChainDialog = false
//                viewModel.leaveSyncChain()
//            },
//        )
    }
}

@Composable
fun PushScreen(
    viewState: PushScreenViewState,
    targetScreen: PushScreenType,
    previousScreen: PushScreenType?,
    onLeavePushSettings: () -> Unit,
    onLeaveAddDevice: () -> Unit,
    onLeavePushJoin: () -> Unit,
    onJoinSyncChain: (ProtoDevices) -> Unit,
    onAddNewDevice: () -> Unit,
    onDeleteDevice: (KnownDevice) -> Unit,
    onScanPushCode: () -> Unit,
    onSelectedDistributor: (String) -> Unit,
    onLeaveSyncChain: () -> Unit,
) {
    if (targetScreen == PushScreenType.DUAL) {
        DualPushScreen(
            onNavigateUp = onLeavePushSettings,
            leftScreenToShow = viewState.leftScreenToShow,
            rightScreenToShow = viewState.rightScreenToShow,
            onScanPushCode = onScanPushCode,
            distributors = ImmutableHolder(viewState.allDistributors),
            selectedDistributor = viewState.currentDistributor,
            onSelectedDistributor = onSelectedDistributor,
            onAddNewDevice = onAddNewDevice,
            onDeleteDevice = onDeleteDevice,
            thisDevice = viewState.thisDevice,
            devices = ImmutableHolder(viewState.knownDevices),
            addDeviceUrl = ImmutableHolder(URL("http://todo")),
            onJoinSyncChain = onJoinSyncChain,
            onLeaveSyncChain = onLeaveSyncChain,
        )
    }

    AnimatedVisibility(
        visible = targetScreen == PushScreenType.SINGLE_SETUP,
        enter = when (previousScreen) {
            null -> fadeIn(initialAlpha = 1.0f)
            else -> fadeIn()
        },
        /*
        This may seem weird - but it's a special case. This exit animation actually runs
        when the first screen is device list. So to prevent a flicker effect it's important to block
        sideways movement. The setup screen will be momentarily on screen because it takes
        a few millis to fetch the push remote.
         */
        exit = when (previousScreen) {
            null -> fadeOut(targetAlpha = 1.0f)
            else -> fadeOut()
        },
    ) {
        PushSetupScreen(
            onNavigateUp = onLeavePushSettings,
            onScanPushCode = onScanPushCode,
            thisDevice = viewState.thisDevice,
            devices = ImmutableHolder(viewState.knownDevices),
            distributors = ImmutableHolder(viewState.allDistributors),
            selectedDistributor = viewState.currentDistributor,
            onSelectedDistributor = onSelectedDistributor,
            onLeaveSyncChain = onLeaveSyncChain,
            onDeleteDevice = onDeleteDevice,
        )
    }

    AnimatedVisibility(
        visible = targetScreen == PushScreenType.SINGLE_JOIN,
        enter = fadeIn(),
        exit = fadeOut(),
    ) {
        PushJoinScreen(
            onNavigateUp = onLeavePushJoin,
            onJoinSyncChain = onJoinSyncChain,
            onLeaveSyncChain = onLeaveSyncChain,
        )
    }

    AnimatedVisibility(
        visible = targetScreen == PushScreenType.SINGLE_DEVICELIST,
        enter = when (previousScreen) {
            PushScreenType.SINGLE_ADD_DEVICE -> fadeIn()
            null -> fadeIn(initialAlpha = 1.0f)
            else -> fadeIn()
        },
        exit = fadeOut(),
    ) {
        PushDeviceListScreen(
            onNavigateUp = onLeavePushSettings,
            thisDevice = viewState.thisDevice,
            devices = ImmutableHolder(viewState.knownDevices),
            onAddNewDevice = onAddNewDevice,
            onDeleteDevice = onDeleteDevice,
            onLeaveSyncChain = onLeaveSyncChain,
        )
    }

    AnimatedVisibility(
        visible = targetScreen == PushScreenType.SINGLE_ADD_DEVICE,
        enter = fadeIn(),
        exit = fadeOut(),
    ) {
        PushAddNewDeviceScreen(
            onNavigateUp = onLeaveAddDevice,
            pushUrl = ImmutableHolder(URL("http://TODO")),
            onLeaveSyncChain = onLeaveSyncChain,
        )
    }
}

enum class PushScreenType {
    DUAL,
    SINGLE_SETUP,
    SINGLE_DEVICELIST,
    SINGLE_ADD_DEVICE,
    SINGLE_JOIN,
}

fun getPushScreenType(
    windowSize: WindowSize,
    viewState: PushScreenViewState
): PushScreenType = when (getScreenType(windowSize)) {
    ScreenType.SINGLE -> {
        when (viewState.singleScreenToShow) {
            PushScreenToShow.SETUP -> PushScreenType.SINGLE_SETUP
            PushScreenToShow.DEVICELIST -> PushScreenType.SINGLE_DEVICELIST
            PushScreenToShow.ADD_DEVICE -> PushScreenType.SINGLE_ADD_DEVICE
            PushScreenToShow.JOIN -> PushScreenType.SINGLE_JOIN
        }
    }
    ScreenType.DUAL -> PushScreenType.DUAL
}

@Composable
fun DualPushScreen(
    onNavigateUp: () -> Unit,
    leftScreenToShow: LeftScreenToShow,
    rightScreenToShow: RightScreenToShow,
    onScanPushCode: () -> Unit,
    distributors: ImmutableHolder<List<String>>,
    selectedDistributor: String,
    onSelectedDistributor: (String) -> Unit,
    onAddNewDevice: () -> Unit,
    onDeleteDevice: (KnownDevice) -> Unit,
    thisDevice: ThisDevice?,
    devices: ImmutableHolder<List<KnownDevice>>,
    addDeviceUrl: ImmutableHolder<URL>,
    onJoinSyncChain: (ProtoDevices) -> Unit,
    onLeaveSyncChain: () -> Unit,
) {
    BackHandler(onBack = onNavigateUp)

    val scrollState = rememberScrollState()

    LaunchedEffect(key1 = leftScreenToShow) {
        scrollState.scrollTo(0)
    }

    PushScaffold(
        leavePushVisible = leftScreenToShow == LeftScreenToShow.DEVICELIST,
        title = stringResource(id = R.string.device_sync),
        onNavigateUp = onNavigateUp,
        onLeaveSyncChain = onLeaveSyncChain,
    ) { modifier ->
        Row(
            modifier = modifier
                .verticalScroll(scrollState),
        ) {
            when (leftScreenToShow) {
                LeftScreenToShow.SETUP -> {
                    PushSetupContent(
                        onScanPushCode = onScanPushCode,
                        distributors = distributors,
                        thisDevice = thisDevice,
                        devices = devices,
                        selectedDistributor = selectedDistributor,
                        onSelectedDistributor = onSelectedDistributor,
                        onDeleteDevice = onDeleteDevice,
                        modifier = Modifier
                            .weight(1f, fill = true)
                    )
                }
                LeftScreenToShow.DEVICELIST -> {
                    PushDeviceListContent(
                        thisDevice = thisDevice,
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
                    PushAddNewDeviceContent(
                        pushUrl = addDeviceUrl,
                        modifier = Modifier
                            .weight(1f, fill = true)
                    )
                }
                RightScreenToShow.JOIN -> {
                    PushJoinContent(
                        modifier = Modifier
                            .weight(1f, fill = true),
                        onJoinSyncChain = onJoinSyncChain,
                    )
                }
            }
        }
    }
}

@Composable
fun PushSetupScreen(
    onNavigateUp: () -> Unit,
    onScanPushCode: () -> Unit,
    thisDevice: ThisDevice?,
    devices: ImmutableHolder<List<KnownDevice>>,
    distributors: ImmutableHolder<List<String>>,
    selectedDistributor: String,
    onSelectedDistributor: (String) -> Unit,
    onLeaveSyncChain: () -> Unit,
    onDeleteDevice: (KnownDevice) -> Unit,
) {
    BackHandler(onBack = onNavigateUp)
    val scrollState = rememberScrollState()

    PushScaffold(
        leavePushVisible = false,
        title = stringResource(id = R.string.device_sync),
        onNavigateUp = onNavigateUp,
        onLeaveSyncChain = onLeaveSyncChain,
    ) { modifier ->
        PushSetupContent(
            modifier = modifier.verticalScroll(scrollState),
            onScanPushCode = onScanPushCode,
            thisDevice = thisDevice,
            devices = devices,
            distributors = distributors,
            selectedDistributor = selectedDistributor,
            onSelectedDistributor = onSelectedDistributor,
            onDeleteDevice = onDeleteDevice,
        )
    }
}

@Composable
fun PushSetupContent(
    modifier: Modifier,
    onScanPushCode: () -> Unit,
    thisDevice: ThisDevice?,
    devices: ImmutableHolder<List<KnownDevice>>,
    distributors: ImmutableHolder<List<String>>,
    selectedDistributor: String,
    onSelectedDistributor: (String) -> Unit,
    onDeleteDevice: (KnownDevice) -> Unit,
) {
    val dimens = LocalDimens.current

    var itemToDelete by remember {
        mutableStateOf<KnownDevice?>(null)
    }

    val enabled by remember(selectedDistributor) {
        derivedStateOf {
            selectedDistributor.isNotEmpty()
        }
    }

    val qrCode by remember(thisDevice) {
        derivedStateOf {
            thisDevice?.let { thisDevice ->
                QRCode.from(
                    Base64.encodeToString(
                        ProtoDevices(
                            devices = devices.item.map { it.toProto() }
                                .plus(
                                    Device(
                                        endpoint = thisDevice.endpoint,
                                        name = thisDevice.name,
                                    )
                                )
                        ).encode(),
                        BASE64_QR_FLAGS,
                    )
                )
                    // TODO match other size
                    .withSize(250, 250)
                    .bitmap()
                    .asImageBitmap()
            }
        }
    }

    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = dimens.margin, vertical = 8.dp)
    ) {
        Text(
            text = stringResource(R.string.device_sync_description_1),
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.fillMaxWidth(),
        )
        Text(
            text = stringResource(R.string.device_sync_description_2),
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.fillMaxWidth(),
        )
        Text(
            text = stringResource(R.string.device_sync_financed_by_community),
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.fillMaxWidth(),
        )
        // Google Play does not allow direct donation links
        if (!BuildConfig.BUILD_TYPE.contains("play", ignoreCase = true)) {
            val context = LocalContext.current
            Text(
                text = KOFI_URL,
                style = MaterialTheme.typography.bodyLarge.merge(LinkTextStyle()),
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
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.fillMaxWidth(),
        )

        Divider(modifier = Modifier.fillMaxWidth())
        Spacer(modifier = Modifier.size(24.dp))

        if (distributors.item.isNotEmpty()) {
            MenuSetting(
                title = "UnifiedPush distributor",
                currentValue = selectedDistributor,
                values = distributors,
                onSelection = onSelectedDistributor,
            )

            Text(
                text = stringResource(R.string.devices_on_sync_chain),
                style = MaterialTheme.typography.titleMedium.copy(color = MaterialTheme.colorScheme.primary),
                modifier = Modifier
                    .padding(top = 8.dp, bottom = 8.dp)
                    .fillMaxWidth()
            )
            if (thisDevice != null) {
                ThisDeviceEntry(thisDevice = thisDevice)
            }
            for (device in devices.item) {
                DeviceEntry(
                    device = device,
                    onDelete = {
                        itemToDelete = device
                    }
                )
            }

            qrCode?.let { qrCode ->
                Image(
                    bitmap = qrCode,
                    contentDescription = stringResource(R.string.qr_code),
                    contentScale = ContentScale.Fit,
                    // TODO dynamic size
                    modifier = Modifier.size(250.dp)
                )
            }

            if (thisDevice != null) {
                Button(
                    onClick = onScanPushCode,
                ) {
                    Text(
                        text = stringResource(id = R.string.scan_or_enter_code)
                    )
                }
            }
        } else {
            val ntfyLink = when (BuildConfig.BUILD_TYPE.contains("play", ignoreCase = true)) {
                true -> "https://play.google.com/store/apps/details?id=io.heckel.ntfy"
                else -> "https://f-droid.org/en/packages/io.heckel.ntfy/"
            }
            val context = LocalContext.current
            Text(
                text = AnnotatedString.Builder().apply {
                    append("You don't seem to have any UnifiedPush distributors installed. Please install one before continuing. You can use any UnifiedPush distributor you like but a good option is ")
                    pushStyle(LinkTextStyle().toSpanStyle())
                    append(ntfyLink)
                }.toAnnotatedString(),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        context.startActivity(openUrlIntent(ntfyLink))
                    },
            )
        }
    }

    itemToDelete?.let {
        DeleteDeviceDialog(
            deviceName = it.name,
            onDismiss = { itemToDelete = null }
        ) {
            onDeleteDevice(it)
            itemToDelete = null
        }
    }
}

internal val String.pushCodeQueryParam
    get() = substringAfter("push_code=").take(64)

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
fun PushJoinScreen(
    onNavigateUp: () -> Unit,
    onJoinSyncChain: (ProtoDevices) -> Unit,
    onLeaveSyncChain: () -> Unit,
) {
    BackHandler(onBack = onNavigateUp)
    val scrollState = rememberScrollState()

    PushScaffold(
        leavePushVisible = false,
        title = stringResource(id = R.string.join_sync_chain),
        onNavigateUp = onNavigateUp,
        onLeaveSyncChain = onLeaveSyncChain,
    ) { modifier ->
        PushJoinContent(
            modifier = modifier.verticalScroll(scrollState),
            onJoinSyncChain = onJoinSyncChain,
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PushJoinContent(
    modifier: Modifier,
    onJoinSyncChain: (ProtoDevices) -> Unit,
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
            value = "",
            label = {
                Text(text = "ENDPOINT")
            },
            onValueChange = { /* TODO */ },
            isError = false,
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 64.dp)
        )
        TextField(
            value = "TODO",
            label = {
                Text(text = stringResource(R.string.secret_key))
            },
            onValueChange = { /* TODO */ },
            isError = false,
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 64.dp)
        )
        Button(
            enabled = false,
            onClick = {
                // TODO
            }
        ) {
            Text(
                text = stringResource(R.string.join_sync_chain),
                style = MaterialTheme.typography.labelLarge,
            )
        }
    }
}

@Composable
fun PushDeviceListScreen(
    onNavigateUp: () -> Unit,
    thisDevice: ThisDevice?,
    devices: ImmutableHolder<List<KnownDevice>>,
    onAddNewDevice: () -> Unit,
    onDeleteDevice: (KnownDevice) -> Unit,
    onLeaveSyncChain: () -> Unit,
) {
    BackHandler(onBack = onNavigateUp)
    val scrollState = rememberScrollState()

    PushScaffold(
        leavePushVisible = true,
        title = stringResource(id = R.string.device_sync),
        onNavigateUp = onNavigateUp,
        onLeaveSyncChain = onLeaveSyncChain,
    ) { modifier ->
        PushDeviceListContent(
            modifier = modifier.verticalScroll(scrollState),
            thisDevice = thisDevice,
            devices = devices,
            onAddNewDevice = onAddNewDevice,
            onDeleteDevice = onDeleteDevice,
            showAddDeviceButton = true,
        )
    }
}

@Composable
fun PushDeviceListContent(
    modifier: Modifier,
    thisDevice: ThisDevice?,
    devices: ImmutableHolder<List<KnownDevice>>,
    onAddNewDevice: () -> Unit,
    onDeleteDevice: (KnownDevice) -> Unit,
    showAddDeviceButton: Boolean,
) {
    var itemToDelete by remember {
        mutableStateOf<KnownDevice?>(null)
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
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier
                .padding(top = 8.dp, bottom = 8.dp)
                .fillMaxWidth()
        )
        for (device in devices.item) {
            DeviceEntry(
                device = device,
                onDelete = {
                    itemToDelete = device
                }
            )
        }
        Text(
            text = stringResource(R.string.device_sync_financed_by_community),
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
        )
        // Google Play does not allow direct donation links
        if (!BuildConfig.BUILD_TYPE.contains("play", ignoreCase = true)) {
            val context = LocalContext.current
            Text(
                text = KOFI_URL,
                style = MaterialTheme.typography.bodyLarge.merge(LinkTextStyle()),
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
                    style = MaterialTheme.typography.labelLarge,
                    textAlign = TextAlign.Center,
                )
            }
        }
        Spacer(modifier = Modifier.size(8.dp))
    }

    itemToDelete?.let {
        DeleteDeviceDialog(
            deviceName = it.name,
            onDismiss = { itemToDelete = null }
        ) {
            onDeleteDevice(it)
            itemToDelete = null
        }
    }
}

@Composable
fun ThisDeviceEntry(
    thisDevice: ThisDevice,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = minimumTouchSize)
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.weight(1f, fill = true)
        ) {
            Text(
                text = thisDevice.name,
                style = MaterialTheme.typography.titleMedium,
                maxLines = 1,
            )
            Text(
                text = "Current device",
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 1,
            )
        }
    }
}

@Composable
fun DeviceEntry(
    device: KnownDevice,
    onDelete: () -> Unit,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = minimumTouchSize)
    ) {
        val text = device.name
        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.weight(1f, fill = true)
        ) {
            Text(
                text = text,
                style = MaterialTheme.typography.titleMedium,
                maxLines = 1,
            )
            Text(
                text = device.lastSeen.toString(),
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 1,
            )
        }
        IconButton(onClick = onDelete) {
            Icon(
                Icons.Filled.Delete,
                contentDescription = stringResource(R.string.delete_device),
            )
        }
    }
}

@Preview
@Composable
fun PreviewDeviceEntry() {
    FeederTheme {
        Surface {
            DeviceEntry(
                device = KnownDevice(
                    endpoint = "",
                    name = "ONEPLUS A6003",
                    lastSeen = Instant.ofEpochSecond(1670360891)
                ),
                onDelete = {},
            )
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
                style = MaterialTheme.typography.titleLarge,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .padding(vertical = 8.dp)
            )
        },
        text = {
            Text(
                text = stringResource(R.string.remove_device_question, deviceName),
                style = MaterialTheme.typography.bodyLarge,
            )
        }
    )
}

@Composable
fun PushAddNewDeviceScreen(
    onNavigateUp: () -> Unit,
    pushUrl: ImmutableHolder<URL>,
    onLeaveSyncChain: () -> Unit,
) {
    BackHandler(onBack = onNavigateUp)
    val scrollState = rememberScrollState()

    PushScaffold(
        leavePushVisible = false,
        onNavigateUp = onNavigateUp,
        onLeaveSyncChain = onLeaveSyncChain,
        title = stringResource(id = R.string.add_new_device),
    ) { modifier ->
        PushAddNewDeviceContent(
            modifier = modifier.verticalScroll(scrollState),
            pushUrl = pushUrl,
        )
    }
}

@Composable
fun PushAddNewDeviceContent(
    modifier: Modifier,
    pushUrl: ImmutableHolder<URL>,
) {
    val context = LocalContext.current

    val qrCode by remember(pushUrl) {
        derivedStateOf {
            QRCode.from(
                Url().also {
                    it.url = "${pushUrl.item}"
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
    ) {
        Text(
            text = stringResource(R.string.press_scan_sync),
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier
                .fillMaxWidth(),
        )
        Text(
            text = stringResource(R.string.or_open_device_sync_link),
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier
                .fillMaxWidth(),
        )
        Text(
            text = stringResource(R.string.treat_like_password),
            style = MaterialTheme.typography.bodyLarge.copy(color = Color.Red),
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
            text = "$pushUrl",
            style = LinkTextStyle(),
            modifier = Modifier
                .fillMaxWidth()
                .clickable {
                    val intent = Intent.createChooser(
                        Intent(Intent.ACTION_SEND).apply {
                            putExtra(Intent.EXTRA_TEXT, "$pushUrl")
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
fun PreviewDualPushScreenDeviceList() {
    FeederTheme {
        DualPushScreen(
            onNavigateUp = { },
            leftScreenToShow = LeftScreenToShow.DEVICELIST,
            rightScreenToShow = RightScreenToShow.ADD_DEVICE,
            onScanPushCode = { },
            onAddNewDevice = { },
            onDeleteDevice = {},
            onLeaveSyncChain = {},
            thisDevice = ThisDevice(endpoint = "foo", name = "Mario's Phone"),
            devices = ImmutableHolder(
                listOf(
                    KnownDevice(endpoint = "", name = "ONEPLUS A6003", lastSeen = Instant.EPOCH),
                    KnownDevice(
                        endpoint = "",
                        name = "SM-T970",
                        lastSeen = Instant.ofEpochSecond(1670360891)
                    ),
                    KnownDevice(
                        endpoint = "",
                        name = "Nexus 6",
                        lastSeen = Instant.ofEpochSecond(1670263925)
                    ),
                )
            ),
            addDeviceUrl = ImmutableHolder(URL("$DEEP_LINK_BASE_URI/push/join?push_code=123foo")),
            onJoinSyncChain = {},
            selectedDistributor = "",
            onSelectedDistributor = {},
            distributors = ImmutableHolder(listOf("Foo", "Bar")),
        )
    }
}

@Preview("Setup Tablet", device = Devices.PIXEL_C)
@Preview("Setup Foldable", device = Devices.FOLDABLE, widthDp = 720, heightDp = 360)
@Composable
fun PreviewDualPushScreenSetup() {
    FeederTheme {
        DualPushScreen(
            onNavigateUp = { },
            leftScreenToShow = LeftScreenToShow.SETUP,
            rightScreenToShow = RightScreenToShow.JOIN,
            onScanPushCode = { },
            onAddNewDevice = { },
            onDeleteDevice = {},
            onLeaveSyncChain = {},
            thisDevice = ThisDevice(endpoint = "foo", name = "Mario's Phone"),
            devices = ImmutableHolder(
                listOf(
                    KnownDevice(endpoint = "", name = "ONEPLUS A6003", lastSeen = Instant.EPOCH),
                    KnownDevice(
                        endpoint = "",
                        name = "SM-T970",
                        lastSeen = Instant.ofEpochSecond(1670360891)
                    ),
                    KnownDevice(
                        endpoint = "",
                        name = "Nexus 6",
                        lastSeen = Instant.ofEpochSecond(1670263925)
                    ),
                )
            ),
            addDeviceUrl = ImmutableHolder(URL("$DEEP_LINK_BASE_URI/push/join?push_code=123foo&key=123ABF")),
            onJoinSyncChain = {},
            onSelectedDistributor = {},
            selectedDistributor = "",
            distributors = ImmutableHolder(listOf("Foo", "Bar")),
        )
    }
}

@Preview("Scan or Enter Phone")
@Preview("Scan or Enter Small Tablet", device = Devices.NEXUS_7_2013)
@Composable
fun PreviewJoin() {
    FeederTheme {
        PushJoinScreen(
            onNavigateUp = {},
            onJoinSyncChain = {},
            onLeaveSyncChain = {},
        )
    }
}

@Preview("Empty Phone")
@Preview("Empty Small Tablet", device = Devices.NEXUS_7_2013)
@Composable
fun PreviewEmpty() {
    FeederTheme {
        PushSetupScreen(
            onNavigateUp = {},
            onScanPushCode = {},
            onLeaveSyncChain = {},
            thisDevice = null,
            devices = ImmutableHolder(emptyList()),
            distributors = ImmutableHolder(listOf("Foo", "Bar")),
            selectedDistributor = "",
            onSelectedDistributor = {},
            onDeleteDevice = {},
        )
    }
}

@Preview("Device List Phone")
@Preview("Device List Small Tablet", device = Devices.NEXUS_7_2013)
@Composable
fun PreviewDeviceList() {
    FeederTheme {
        PushDeviceListScreen(
            onNavigateUp = {},
            thisDevice = ThisDevice(endpoint = "foo", name = "Mario's Phone"),
            devices = ImmutableHolder(
                listOf(
                    KnownDevice(endpoint = "", name = "ONEPLUS A6003", lastSeen = Instant.EPOCH),
                    KnownDevice(
                        endpoint = "",
                        name = "SM-T970",
                        lastSeen = Instant.ofEpochSecond(1670360891)
                    ),
                    KnownDevice(
                        endpoint = "",
                        name = "Nexus 6",
                        lastSeen = Instant.ofEpochSecond(1670263925)
                    ),
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
        PushAddNewDeviceScreen(
            onNavigateUp = {},
            onLeaveSyncChain = {},
            pushUrl = ImmutableHolder(URL("https://feeder-push.nononsenseapps.com/join?push_code=1234abc572335asdbc&key=123ABF")),
        )
    }
}
