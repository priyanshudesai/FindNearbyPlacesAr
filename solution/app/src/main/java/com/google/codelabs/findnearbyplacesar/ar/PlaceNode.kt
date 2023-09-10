// Copyright 2020 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//      http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.google.codelabs.findnearbyplacesar.ar

import android.content.Context
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.Glide
import com.google.ar.core.Pose
import com.google.ar.sceneform.Node
import com.google.ar.sceneform.math.Quaternion
import com.google.ar.sceneform.math.Vector3
import com.google.ar.sceneform.rendering.ViewRenderable
import com.google.codelabs.findnearbyplacesar.R
import com.google.codelabs.findnearbyplacesar.model.Place

class PlaceNode(
    val context: Context,
    val place: Place?
) : Node() {

    private var placeRenderable: ViewRenderable? = null
    private var textViewPlace: TextView? = null

    override fun onActivate() {
        super.onActivate()

        if (scene == null) {
            return
        }

        if (placeRenderable != null) {
            return
        }

        ViewRenderable.builder()
            .setView(context, R.layout.place_view)
            .build()
            .thenAccept { renderable ->
                setRenderable(renderable)
                placeRenderable = renderable

                place?.let {
                    textViewPlace = renderable.view.findViewById(R.id.placeName)
                    val imgPlace = renderable.view.findViewById<ImageView>(R.id.imgIcon)

                    textViewPlace?.text = it.name
                    Glide.with(context)
                        .load(it.icon)
                        .centerCrop()
                        .placeholder(R.mipmap.pin_full_color)
                        .error(R.mipmap.pin_full_color)
                        .into(imgPlace)
                }
            }
    }

    fun showInfoWindow() {
        // Show text
        textViewPlace?.let {
            it.visibility = if (it.visibility == View.VISIBLE) View.GONE else View.VISIBLE
        }

        // Hide text for other nodes
        this.parent?.children?.filter {
            it is PlaceNode && it != this
        }?.forEach {
            (it as PlaceNode).textViewPlace?.visibility = View.GONE
        }
    }

    fun updateMarkerOrientation(anchorPose: Pose?) {
        if (anchorPose == null) return
        // Extract the anchor's rotation as a quaternion
        val anchorRotation = Quaternion(
            anchorPose.qx(),
            anchorPose.qy(),
            anchorPose.qz(),
            anchorPose.qw()
        )

        // Create a new quaternion with zero pitch and roll
        val updatedRotation = Quaternion.axisAngle(Vector3(1f, 0f, 0f), 0f)

        // Apply the corrected rotation to the marker
        localRotation = Quaternion.multiply(updatedRotation, anchorRotation)
    }
}