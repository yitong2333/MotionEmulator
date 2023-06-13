package com.zhufucdev.update

import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UpdaterApp(navigateUp: () -> Unit, updater: Updater, install: suspend (File) -> Boolean) {
    val coroutine = rememberCoroutineScope { Dispatchers.Default }
    val snackbar = remember { SnackbarHostState() }
    val context = LocalContext.current

    LaunchedEffect(updater.update) {
        if (updater.update == null) {
            updater.check()
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            updater.close()
        }
    }

    Scaffold(
        topBar = { AppBar(TopAppBarDefaults.enterAlwaysScrollBehavior(), navigateUp) },
        floatingActionButton = {
            if (updater.status is StatusReadyToDownload) {
                ExtendedFloatingActionButton(
                    onClick = {
                        coroutine.launch {
                            try {
                                updater.download()
                                Log.d("UpdaterApp", "Downloaded")
                            } catch (e: Exception) {
                                snackbar.showSnackbar(context.getString(R.string.text_download_failed, e.message))
                                e.printStackTrace()
                            }
                        }
                    },
                    icon = { Icon(painterResource(R.drawable.ic_baseline_download), contentDescription = null) },
                    text = { Text(stringResource(R.string.title_download)) }
                )
            }
        },
        snackbarHost = { SnackbarHost(snackbar) }
    ) {
        Column(Modifier.padding(it).fillMaxWidth()) {
            Icon(
                painterResource(R.drawable.ic_baseline_update),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.secondary,
                modifier = Modifier.align(Alignment.CenterHorizontally).size(30.dp)
            )
            Spacer(Modifier.height(12.dp))

            val title =
                when (updater.status) {
                    is StatusReadyToInstall -> stringResource(R.string.title_ready_to_install)
                    is Downloading -> stringResource(R.string.title_downloading)
                    is HasUpdate -> stringResource(R.string.text_new_version)
                    is StatusGenericWorking -> stringResource(R.string.text_looking)
                    else -> stringResource(R.string.text_no_update)
                }
            Text(
                title,
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )

            Box(Modifier.fillMaxWidth().padding(12.dp)) {
                val status = updater.status
                if (status is StatusDownloading) {
                    val progress by status.progress
                    LinearProgressIndicator(
                        modifier = Modifier.fillMaxWidth(),
                        progress = progress
                    )
                } else if (updater.status is StatusGenericWorking) {
                    LinearProgressIndicator(Modifier.fillMaxWidth())
                }
            }

            AnimatedVisibility(
                visible = updater.status == StatusIdling || updater.status is StatusReadyToInstall,
                enter = fadeIn(),
                exit = fadeOut(),
                modifier = Modifier.align(Alignment.End).padding(end = 12.dp)
            ) {
                when (val status = updater.status) {
                    is StatusIdling -> {
                        Button(
                            onClick = {
                                coroutine.launch {
                                    updater.check()
                                }
                            },
                            content = {
                                Text(stringResource(R.string.action_rerun))
                            },
                        )
                    }

                    is StatusReadyToInstall -> {
                        Button(
                            onClick = {
                                coroutine.launch {
                                    val success = install(status.file)
                                    if (!success) {
                                        val action = snackbar.showSnackbar(
                                            message = context.getString(R.string.text_unable_to_install_update),
                                            actionLabel = context.getString(R.string.action_reveal_in_files)
                                        )
                                        if (action == SnackbarResult.ActionPerformed) {

                                        }
                                    }
                                }
                            },
                            content = {
                                Text(stringResource(R.string.action_install))
                            }
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AppBar(scrollBehavior: TopAppBarScrollBehavior, finish: () -> Unit) {
    LargeTopAppBar(
        title = { Text(stringResource(R.string.title_updater)) },
        scrollBehavior = scrollBehavior,
        navigationIcon = {
            PlainTooltipBox(
                tooltip = { Text(stringResource(R.string.action_navigate_up)) }
            ) {
                IconButton(
                    onClick = { finish() },
                    content = { Icon(Icons.Default.ArrowBack, null) },
                    modifier = Modifier.tooltipTrigger()
                )
            }
        }
    )
}