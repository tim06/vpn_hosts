import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.logging.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.util.*
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.jsoup.Jsoup

private val json = Json { ignoreUnknownKeys = true }

private val client = HttpClient(CIO) {
    install(ContentNegotiation) {
        json()
    }
    install(Logging) {
        level = LogLevel.ALL
        logger = object : Logger {
            override fun log(message: String) {
                print(message)
            }
        }
    }
}

fun main(args: Array<String>) {
    val enviriment = System.getenv("TOKEN")
    val token = when {
        args.isNotEmpty() -> args.first()
        enviriment != null -> enviriment
        else -> throw (NotImplementedError("TOKEN NOT FOUND"))
    }

    val url = "http://150.95.29.30:23357/en/"
    parseFromHtml(url, token)
}

private fun parseFromHtml(url: String, token: String) {
    val initial = Jsoup.connect(url).get()

    val hosts = initial.body().getElementsByTag("table").get(7).getElementsByTag("tr").mapNotNull {
        if (it.child(0).className() == "vg_table_header") {
            null
        } else {
            val countryCode =
                it.child(0).getElementsByTag("img").firstOrNull()?.attr("src")?.split("/")?.lastOrNull()?.split(".")
                    ?.firstOrNull().orEmpty()
            val ip = it.child(1).getElementsByAttributeValue("style", "font-size: 10pt;").text()
            val sessions = it.child(2).getElementsByAttributeValue("style", "font-size: 10pt;").text()
            val name = it.child(7).getElementsByAttributeValue("style", "color: #006600;").text()
            val resultPair = if (name.contains(":")) {
                val splited = name.split(":")
                splited.first() to splited.last()
            } else {
                name to "443"
            }
            if (name.isNullOrEmpty()) {
                null
            } else {
                HostModel(
                    ip = ip,
                    port = resultPair.second,
                    host = resultPair.first,
                    countryCode = countryCode,
                    numberOfSessions = sessions
                )
            }
        }
    }
    sendHosts(hosts, token)
}

private fun sendHosts(hosts: List<HostModel>, token: String) {
    runBlocking {
        val dataToSend = json.encodeToString(hosts).encodeBase64()
        val resultModel = GithubSendModel(
            message = "Hosts update", content = dataToSend, sha = getSha(token)
        )

        val result = client.put("https://api.github.com/repos/tim06/vpn_hosts/contents/hosts.json") {
            contentType(ContentType.Application.Json)
            header("Accept", "application/vnd.github+json")
            header("Authorization", "Bearer $token")
            header("X-GitHub-Api-Version", "2022-11-28")
            setBody(body = resultModel)
        }
        println(result.status)
    }
}

private suspend fun getSha(token: String): String {
    val blob = client.get("https://api.github.com/repos/tim06/vpn_hosts/contents/hosts.json") {
        header("Accept", "application/vnd.github+json")
        header("Authorization", "Bearer $token")
        header("X-GitHub-Api-Version", "2022-11-28")
    }.bodyAsText()
    return json.decodeFromString<GithubFileBlobResponse>(blob).sha
}

@Serializable
data class GithubSendModel(
    val message: String, val content: String, val sha: String
)

@Serializable
data class GithubFileBlobResponse(
    val sha: String
)

@Serializable
data class HostModel(
    val ip: String, val port: String, val host: String, val countryCode: String, val numberOfSessions: String
)