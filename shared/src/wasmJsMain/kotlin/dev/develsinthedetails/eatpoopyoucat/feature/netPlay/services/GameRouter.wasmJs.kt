package dev.develsinthedetails.eatpoopyoucat.feature.netPlay.services

import dev.develsinthedetails.eatpoopyoucat.app.AppSettings
import dev.develsinthedetails.eatpoopyoucat.data.AppRepository

actual class GameRouter actual constructor(
    repository: AppRepository,
    client: Client,
    appSettings: AppSettings
)