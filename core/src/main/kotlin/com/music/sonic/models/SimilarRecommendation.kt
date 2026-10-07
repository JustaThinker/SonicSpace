package com.music.sonic.models

import com.music.innertube.models.YTItem
import com.music.sonic.db.entities.LocalItem

data class SimilarRecommendation(
  val title: LocalItem,
  val items: List<YTItem>,
)
