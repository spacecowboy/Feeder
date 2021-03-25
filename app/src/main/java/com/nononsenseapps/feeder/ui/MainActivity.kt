package com.nononsenseapps.feeder.ui

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.clickable
import androidx.compose.material.DrawerValue
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.rememberDrawerState
import androidx.compose.material.rememberScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.nononsenseapps.feeder.base.KodeinAwareComponentActivity
import com.nononsenseapps.feeder.model.FeedListViewModel
import com.nononsenseapps.feeder.ui.compose.components.FeedList
import com.nononsenseapps.feeder.ui.compose.theme.FeederTheme
import kotlinx.coroutines.launch
import org.kodein.di.direct
import org.kodein.di.generic.instance

@ExperimentalAnimationApi
class MainActivity : KodeinAwareComponentActivity() {
    private val feedListViewModel: FeedListViewModel by viewModels {
        kodein.direct.instance()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val coroutineScope = rememberCoroutineScope()
            val scaffoldState = rememberScaffoldState(
                rememberDrawerState(initialValue = DrawerValue.Open)
            )
            FeederTheme {
                Scaffold(
                    scaffoldState = scaffoldState,
                    topBar = {
                        TopAppBar(
                            title = { Text("Feeder") },
                            navigationIcon = {
                                Icon(
                                    Icons.Default.Menu,
                                    contentDescription = "Drawer toggle",
                                    modifier = Modifier
                                        .clickable {
                                            coroutineScope.launch {
                                                scaffoldState.drawerState.open()
                                            }
                                        }
                                )
                            }
                        )
                    },
                    drawerContent = {
                        FeedList(feedListViewModel)
                    }
                ) {
                    Greeting("Android")
                }
            }
        }
    }
}

@Composable
fun Greeting(name: String) {
    Text(text = "Hello $name!")
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    FeederTheme {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Feeder") },
                    navigationIcon = {
                        Icon(
                            Icons.Default.Menu,
                            contentDescription = "Drawer toggle"
                        )
                    }
                )
            },
            drawerContent = {
                Text("The Drawer")
            }
        ) {
            // A surface container using the 'background' color from the theme
            Surface(color = MaterialTheme.colors.background) {
                Greeting("Android")
            }
        }
    }
}
