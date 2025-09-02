package com.bloo.helloarcore2

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.google.ar.core.Config
import com.google.ar.core.Frame
import io.github.sceneview.ar.ARScene
import io.github.sceneview.ar.arcore.createAnchorOrNull
import io.github.sceneview.ar.arcore.isValid
import io.github.sceneview.ar.node.AnchorNode
import io.github.sceneview.ar.rememberARCameraNode
import io.github.sceneview.loaders.MaterialLoader
import io.github.sceneview.math.Position
import io.github.sceneview.math.Size
import io.github.sceneview.node.CubeNode
import io.github.sceneview.rememberCollisionSystem
import io.github.sceneview.rememberEngine
import io.github.sceneview.rememberMaterialLoader
import io.github.sceneview.rememberModelLoader
import io.github.sceneview.rememberNodes
import io.github.sceneview.rememberOnGestureListener
import io.github.sceneview.rememberView

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            Box(modifier = Modifier.fillMaxSize()) {
                val engine = rememberEngine()
                val modelLoader = rememberModelLoader(engine)
                val materialLoader = rememberMaterialLoader(engine)
                val cameraNode = rememberARCameraNode(engine)
                val childNodes = rememberNodes()
                val view = rememberView(engine)
                val collisionSystem = rememberCollisionSystem(view)

                var frame by remember { mutableStateOf<Frame?>(null) }
                var planeRenderer by remember { mutableStateOf(true) }

                ARScene(
                    modifier = Modifier.fillMaxSize(),
                    childNodes = childNodes,
                    engine = engine,
                    view = view,
                    modelLoader = modelLoader,
                    collisionSystem = collisionSystem,
                    sessionConfiguration = { session, config ->
                        config.depthMode =
                            when (session.isDepthModeSupported(Config.DepthMode.AUTOMATIC)) {
                                true -> Config.DepthMode.AUTOMATIC
                                else -> Config.DepthMode.DISABLED
                            }
                        config.lightEstimationMode = Config.LightEstimationMode.ENVIRONMENTAL_HDR
                    },
                    cameraNode = cameraNode,
                    planeRenderer = planeRenderer,
                    onSessionUpdated = { _, updatedFrame ->
                        frame = updatedFrame
                    },
                    onGestureListener = rememberOnGestureListener(
                        onSingleTapConfirmed = { motionEvent, _ ->
                            val hitResults = frame?.hitTest(motionEvent.x, motionEvent.y)
                            hitResults?.firstOrNull { it.isValid(depthPoint = false, point = false) }
                                ?.createAnchorOrNull()
                                ?.let { anchor ->
                                    val anchorNode = AnchorNode(engine, anchor)
                                    val cubeNode = CubeNode(
                                        engine = engine,
                                        size = Size(0.2f),
                                        center = Position(y = 0.1f),
                                        materialInstance = materialLoader.createColorInstance(
                                            androidx.compose.ui.graphics.Color.Blue
                                        )
                                    )
                                    anchorNode.addChildNode(cubeNode)
                                    childNodes.add(anchorNode)
                                    if (planeRenderer) {
                                        planeRenderer = false
                                    }
                                }
                        }
                    )
                )
            }
        }
    }
}