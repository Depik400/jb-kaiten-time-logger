package com.depik400.kaitentimelogger.services

import com.google.gson.Gson
import com.google.gson.JsonObject
import com.intellij.openapi.components.Service
import com.intellij.openapi.diagnostic.logger
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.Messages
import com.depik400.kaitentimelogger.models.TimeLogData
import com.intellij.openapi.application.ApplicationManager
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException

@Service(Service.Level.PROJECT)
class KaitenApiService(private val project: Project) {

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
        .readTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
        .build()

    private val gson = Gson()
    private val LOG = logger<KaitenApiService>()

    fun sendTimeLog(data: TimeLogData) {
        val settings = SettingsService.getInstance()
        val baseUrl = settings.state.baseUrl
        val token = settings.state.apiToken

        if (baseUrl.isBlank() || token.isBlank()) {
            ApplicationManager.getApplication().invokeLater {
                Messages.showWarningDialog(
                    project,
                    "Настройки не заполнены. Пожалуйста, настройте расширение:\n" +
                            "Settings → Tools → Kaiten Time Logger",
                    "Kaiten Time Logger"
                )
            }
            return
        }

        val url = "$baseUrl/api/latest/cards/${data.cardId}/time-logs"

        val payload = JsonObject().apply {
            addProperty("role_id", data.roleId)
            addProperty("time_spent", data.timeSpent)
            addProperty("for_date", data.forDate)
            addProperty("comment", data.comment)
        }

        val jsonPayload = gson.toJson(payload)
        LOG.info("Sending request to: $url")
        LOG.info("Payload: $jsonPayload")

        val request = Request.Builder()
            .url(url)
            .post(jsonPayload.toRequestBody("application/json; charset=utf-8".toMediaType()))
            .header("Authorization", "Bearer $token")
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                LOG.error("Request failed", e)
                Messages.showErrorDialog(
                    project,
                    "Ошибка: ${e.message}\nПроверьте соединение и настройки.",
                    "Kaiten Time Logger"
                )
            }

            override fun onResponse(call: Call, response: Response) {
                val responseBody = response.body?.string() ?: ""
                LOG.info("Response code: ${response.code}")
                LOG.info("Response body: $responseBody")

                if (response.isSuccessful) {
                    Messages.showInfoMessage(
                        project,
                        "✅ Время по задаче #${data.cardId} успешно залогировано!",
                        "Kaiten Time Logger"
                    )
                } else {
                    val errorMsg = when (response.code) {
                        400 -> "Ошибка валидации данных"
                        401 -> "Неверный токен"
                        403 -> "Доступ запрещен"
                        404 -> "Карточка не найдена"
                        else -> "Ошибка ${response.code}"
                    }

                    Messages.showErrorDialog(
                        project,
                        "Ошибка: $errorMsg\n$responseBody",
                        "Kaiten Time Logger"
                    )
                }
                response.close()
            }
        })
    }

// В KaitenApiService.kt добавить:

    fun getUserRoles(callback: (List<Role>) -> Unit) {
        val state = SettingsService.getInstance().getState()
        val baseUrl = state.baseUrl
        val token = state.apiToken
        if (baseUrl.isBlank() || token.isBlank()) {
            callback(emptyList())
            return
        }
        val url = "$baseUrl/api/latest/user-roles"
        val request = Request.Builder()
            .url(url)
            .get()
            .header("Authorization", "Bearer $token")
            .build()
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                LOG.warn("Failed to load roles", e)
                callback(emptyList())
            }
            override fun onResponse(call: Call, response: Response) {
                val body = response.body?.string()
                if (response.isSuccessful && body != null) {
                    try {
                        val roles = gson.fromJson(body, Array<Role>::class.java).toList()
                        callback(roles)
                    } catch (e: Exception) {
                        LOG.warn("Failed to parse roles", e)
                        callback(emptyList())
                    }
                } else {
                    callback(emptyList())
                }
                response.close()
            }
        })
    }

    data class Role(val id: Int, val name: String, val uid: String? = null)

    companion object {
        fun getInstance(project: Project): KaitenApiService =
            project.getService(KaitenApiService::class.java)
    }
}