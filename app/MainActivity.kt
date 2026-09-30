package com.nextbrowser

import android.annotation.SuppressLint
import android.app.DownloadManager
import android.content.*
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.webkit.*
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.viewmodel.compose.viewModel
import java.net.URLEncoder

private const val HOME = "https://www.google.com"

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent { NextBrowserApp() }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NextBrowserApp(vm: BrowserViewModel = viewModel()) {
    val context = LocalContext.current
    val tabs = remember { mutableStateListOf(BrowserTab(1)) }
    var activeId by remember { mutableIntStateOf(1) }
    var nextId by remember { mutableIntStateOf(2) }
    var address by remember { mutableStateOf(HOME) }
    var progress by remember { mutableFloatStateOf(1f) }
    var showTabs by remember { mutableStateOf(false) }
    var showMenu by remember { mutableStateOf(false) }
    var showBookmarks by remember { mutableStateOf(false) }
    var showHistory by remember { mutableStateOf(false) }
    var showSettings by remember { mutableStateOf(false) }
    var incognito by remember { mutableStateOf(false) }

    val active = tabs.first { it.id == activeId }

    fun navigate(input: String) {
        val url = normalize(input)
        address = url
        active.url = url
    }

    MaterialTheme(
        colorScheme = darkColorScheme(
            primary = Color(0xFF8AB4F8),
            background = Color(0xFF0B0D10),
            surface = Color(0xFF12151A)
        )
    ) {
        Scaffold(
            containerColor = Color(0xFF0B0D10),
            topBar = {
                Column {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .statusBarsPadding()
                            .padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextField(
                            value = address,
                            onValueChange = { address = it },
                            modifier = Modifier.weight(1f).height(54.dp),
                            singleLine = true,
                            shape = RoundedCornerShape(28.dp),
                            leadingIcon = {
                                Icon(
                                    if (address.startsWith("https://")) Icons.Default.Lock
                                    else Icons.Default.Search,
                                    contentDescription = null
                                )
                            },
                            trailingIcon = {
                                IconButton(onClick = { navigate(address) }) {
                                    Icon(Icons.Default.Search, "Go")
                                }
                            },
                            keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                                imeAction = ImeAction.Go
                            ),
                            keyboardActions = androidx.compose.foundation.text.KeyboardActions(
                                onGo = { navigate(address) }
                            ),
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = Color(0xFF20242B),
                                unfocusedContainerColor = Color(0xFF20242B),
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent,
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            )
                        )
                        IconButton(onClick = { showTabs = true }) {
                            BadgedBox(badge = { Badge { Text("${tabs.size}") } }) {
                                Icon(Icons.Default.Tab, "Tabs", tint = Color.White)
                            }
                        }
                        Box {
                            IconButton(onClick = { showMenu = true }) {
                                Icon(Icons.Default.MoreVert, "Menu", tint = Color.White)
                            }
                            DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {
                                DropdownMenuItem(
                                    text = { Text(if (incognito) "Exit Incognito" else "New Incognito Tab") },
                                    onClick = {
                                        incognito = !incognito
                                        if (incognito) {
                                            tabs.add(BrowserTab(nextId++, HOME, "Private tab", true))
                                            activeId = tabs.last().id
                                            address = HOME
                                        }
                                        showMenu = false
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("Bookmarks") },
                                    onClick = { showBookmarks = true; showMenu = false }
                                )
                                DropdownMenuItem(
                                    text = { Text("History") },
                                    onClick = { showHistory = true; showMenu = false }
                                )
                                DropdownMenuItem(
                                    text = { Text("Settings") },
                                    onClick = { showSettings = true; showMenu = false }
                                )
                                DropdownMenuItem(
                                    text = { Text("Add bookmark") },
                                    onClick = {
                                        vm.toggleBookmark(active.title, active.url)
                                        showMenu = false
                                    }
                                )
                            }
                        }
                    }
                    LinearProgressIndicator(
                        progress = { progress.coerceIn(0f, 1f) },
                        modifier = Modifier.fillMaxWidth().height(2.dp),
                        color = MaterialTheme.colorScheme.primary,
                        trackColor = Color.Transparent
                    )
                }
            },
            bottomBar = {
                NavigationBar(containerColor = Color(0xFF12151A)) {
                    NavigationBarItem(
                        selected = false,
                        onClick = { active.webView?.goBack() },
                        icon = { Icon(Icons.Default.ArrowBack, "Back") },
                        label = { Text("Back") }
                    )
                    NavigationBarItem(
                        selected = false,
                        onClick = { active.webView?.goForward() },
                        icon = { Icon(Icons.Default.ArrowForward, "Forward") },
                        label = { Text("Next") }
                    )
                    NavigationBarItem(
                        selected = false,
                        onClick = { active.webView?.loadUrl(HOME) },
                        icon = { Icon(Icons.Default.Home, "Home") },
                        label = { Text("Home") }
                    )
                    NavigationBarItem(
                        selected = false,
                        onClick = { active.webView?.reload() },
                        icon = { Icon(Icons.Default.Refresh, "Reload") },
                        label = { Text("Reload") }
                    )
                }
            }
        ) { padding ->
            Box(Modifier.fillMaxSize().padding(padding)) {
                BrowserWebView(
                    tab = active,
                    onUrlChanged = { address = it; active.url = it },
                    onTitleChanged = { active.title = it },
                    onProgress = { progress = it },
                    onDownload = { url, userAgent, contentDisposition, mime ->
                        enqueueDownload(context, url, userAgent, contentDisposition, mime)
                    },
                    onVisit = { title, url, private -> vm.recordVisit(title, url, private) }
                )
            }
        }

        if (showTabs) {
            TabSheet(
                tabs = tabs,
                activeId = activeId,
                onSelect = {
                    activeId = it.id
                    address = tabs.first { t -> t.id == activeId }.url
                    showTabs = false
                },
                onNew = {
                    tabs.add(BrowserTab(nextId++, HOME, "New tab", incognito))
                    activeId = tabs.last().id
                    address = HOME
                    showTabs = false
                },
                onClose = { id ->
                    if (tabs.size > 1) {
                        tabs.removeAll { it.id == id }
                        if (activeId == id) activeId = tabs.first().id
                        address = tabs.first { it.id == activeId }.url
                    }
                },
                onDismiss = { showTabs = false }
            )
        }

        if (showBookmarks) {
            ListSheet("Bookmarks", vm.bookmarks.collectAsState().value.map { it.title to it.url },
                onSelect = { navigate(it); showBookmarks = false },
                onClear = { vm.clearBookmarks() },
                onDismiss = { showBookmarks = false })
        }

        if (showHistory) {
            ListSheet("History", vm.history.collectAsState().value.map { it.title to it.url },
                onSelect = { navigate(it); showHistory = false },
                onClear = { vm.clearHistory() },
                onDismiss = { showHistory = false })
        }

        if (showSettings) {
            SettingsSheet(
                incognito = incognito,
                onIncognito = { incognito = it },
                onClearHistory = vm::clearHistory,
                onDismiss = { showSettings = false }
            )
        }
    }
}

@Composable
private fun TabSheet(
    tabs: List<BrowserTab>,
    activeId: Int,
    onSelect: (Int) -> Unit,
    onNew: () -> Unit,
    onClose: (Int) -> Unit,
    onDismiss: () -> Unit
) {
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Row(
            Modifier.fillMaxWidth().padding(horizontal = 20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Tabs", style = MaterialTheme.typography.headlineSmall)
            Button(onClick = onNew) { Text("New tab") }
        }
        LazyColumn(Modifier.fillMaxWidth().padding(12.dp)) {
            items(tabs, key = { it.id }) { tab ->
                ListItem(
                    headlineContent = { Text(tab.title.ifBlank { "New tab" }, maxLines = 1) },
                    supportingContent = { Text(tab.url, maxLines = 1) },
                    leadingContent = { Icon(if (tab.incognito) Icons.Default.Lock else Icons.Default.Language, null) },
                    trailingContent = {
                        IconButton(onClick = { onClose(tab.id) }) {
                            Icon(Icons.Default.Close, "Close")
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(if (tab.id == activeId) Color(0xFF20242B) else Color.Transparent)
                        .clickable { onSelect(tab.id) }
                )
            }
        }
    }
}

@Composable
private fun ListSheet(
    title: String,
    items: List<Pair<String, String>>,
    onSelect: (String) -> Unit,
    onClear: () -> Unit,
    onDismiss: () -> Unit
) {
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Row(
            Modifier.fillMaxWidth().padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(title, style = MaterialTheme.typography.headlineSmall)
            TextButton(onClick = onClear) { Text("Clear") }
        }
        if (items.isEmpty()) {
            Box(Modifier.fillMaxWidth().height(180.dp), contentAlignment = Alignment.Center) {
                Text("Nothing here yet")
            }
        } else {
            LazyColumn {
                items(items) { item ->
                    ListItem(
                        headlineContent = { Text(item.first.ifBlank { item.second }, maxLines = 1) },
                        supportingContent = { Text(item.second, maxLines = 1) },
                        modifier = Modifier.clickable { onSelect(item.second) }
                    )
                }
            }
        }
        Spacer(Modifier.height(24.dp))
    }
}

@Composable
private fun SettingsSheet(
    incognito: Boolean,
    onIncognito: (Boolean) -> Unit,
    onClearHistory: () -> Unit,
    onDismiss: () -> Unit
) {
    var blockTrackers by remember { mutableStateOf(true) }
    var darkMode by remember { mutableStateOf(true) }
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Text("Settings", style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.padding(20.dp))
        SettingSwitch("Dark mode", darkMode) { darkMode = it }
        SettingSwitch("Block common trackers", blockTrackers) { blockTrackers = it }
        SettingSwitch("Incognito mode", incognito, onIncognito)
        ListItem(
            headlineContent = { Text("Clear browsing history") },
            leadingContent = { Icon(Icons.Default.DeleteSweep, null) },
            modifier = Modifier.clickable { onClearHistory() }
        )
        Text(
            "Next Browser 1.0 • Privacy controls are local to this device.",
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.padding(20.dp)
        )
        Spacer(Modifier.height(20.dp))
    }
}

@Composable
private fun SettingSwitch(label: String, checked: Boolean, onChange: (Boolean) -> Unit) {
    ListItem(
        headlineContent = { Text(label) },
        trailingContent = { Switch(checked = checked, onCheckedChange = onChange) }
    )
}

@SuppressLint("SetJavaScriptEnabled")
@Composable
private fun BrowserWebView(
    tab: BrowserTab,
    onUrlChanged: (String) -> Unit,
    onTitleChanged: (String) -> Unit,
    onProgress: (Float) -> Unit,
    onDownload: (String, String, String, String) -> Unit,
    onVisit: (String, String, Boolean) -> Unit
) {
    val context = LocalContext.current
    val webView = remember(tab.id) {
        WebView(context).apply {
            settings.javaScriptEnabled = true
            settings.domStorageEnabled = true
            settings.loadsImagesAutomatically = true
            settings.javaScriptCanOpenWindowsAutomatically = false
            settings.setSupportMultipleWindows(false)
            settings.mediaPlaybackRequiresUserGesture = true
            settings.allowFileAccess = false
            settings.allowContentAccess = true
            settings.safeBrowsingEnabled = true
            settings.userAgentString = settings.userAgentString + " NextBrowser/1.0"

            webViewClient = object : WebViewClient() {
                override fun shouldOverrideUrlLoading(
                    view: WebView, request: WebResourceRequest
                ): Boolean = false

                override fun onPageStarted(view: WebView, url: String, favicon: Bitmap?) {
                    onUrlChanged(url)
                }

                override fun onPageFinished(view: WebView, url: String) {
                    onUrlChanged(url)
                    onVisit(view.title ?: url, url, tab.incognito)
                }
            }

            webChromeClient = object : WebChromeClient() {
                override fun onReceivedTitle(view: WebView, title: String) {
                    onTitleChanged(title)
                }

                override fun onProgressChanged(view: WebView, newProgress: Int) {
                    onProgress(newProgress / 100f)
                }
            }

            setDownloadListener { url, userAgent, contentDisposition, mimeType, _ ->
                onDownload(url, userAgent, contentDisposition, mimeType)
            }

            loadUrl(tab.url)
        }
    }

    DisposableEffect(tab.id) {
        tab.webView = webView
        onDispose {
            tab.webView = null
            webView.stopLoading()
            webView.destroy()
        }
    }

    AndroidView(factory = { webView }, modifier = Modifier.fillMaxSize())
}

private fun enqueueDownload(
    context: Context,
    url: String,
    userAgent: String,
    contentDisposition: String,
    mimeType: String
) {
    try {
        val request = DownloadManager.Request(Uri.parse(url))
            .setMimeType(mimeType)
            .addRequestHeader("User-Agent", userAgent)
            .setNotificationVisibility(
                DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED
            )
            .setDestinationInExternalPublicDir(
                Environment.DIRECTORY_DOWNLOADS,
                URLUtil.guessFileName(url, contentDisposition, mimeType)
            )
        context.getSystemService(DownloadManager::class.java).enqueue(request)
    } catch (_: Exception) {
        // Download errors are intentionally non-fatal to browsing.
    }
}

private fun normalize(input: String): String {
    val value = input.trim()
    if (value.isBlank()) return HOME
    if (value.startsWith("http://") || value.startsWith("https://")) return value
    return if (value.contains(".") && !value.contains(" ")) {
        "https://$value"
    } else {
        "https://www.google.com/search?q=" + URLEncoder.encode(value, "UTF-8")
    }
}

private var BrowserTab.webView: WebView?
    get() = BrowserWebViewRegistry.get(id)
    set(value) = BrowserWebViewRegistry.put(id, value)

private object BrowserWebViewRegistry {
    private val map = mutableMapOf<Int, WebView>()
    fun get(id: Int) = map[id]
    fun put(id: Int, value: WebView?) {
        if (value == null) map.remove(id) else map[id] = value
    }
}
