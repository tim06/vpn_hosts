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

fun main(args: Array<String>) {
    val token: String = System.getenv("TOKEN") ?: return

    val url = "https://www.vpngate.net/en/"
    val initial = Jsoup.connect(url).get()

    val hosts = initial.body().getElementsByTag("table").get(7).getElementsByTag("tr").mapNotNull {
        val ip = it.child(1).getElementsByAttributeValue("style", "font-size: 10pt;").text()
        val name = it.child(7).getElementsByAttributeValue("style", "color: #006600;").text()
        if (ip.isNullOrEmpty() || name.isNullOrEmpty()) {
            null
        } else {
            Host(ip = ip, name = name)
        }
    }
    sendHosts(hosts, token)
}

private val json = Json { ignoreUnknownKeys = true }

private fun sendHosts(hosts: List<Host>, token: String) {
    val client = HttpClient(CIO) {
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

    runBlocking {
        val blob = client.get("https://api.github.com/repos/tim06/vpn_hosts/contents/hosts.json") {
            header("Accept", "application/vnd.github+json")
            header("Authorization", "Bearer $token")
            header("X-GitHub-Api-Version", "2022-11-28")
        }.bodyAsText()

        val sha = json.decodeFromString<GithubFileBlobResponse>(blob)

        val dataToSend = json.encodeToString(hosts).encodeBase64()
        val resultModel = GithubModel(
            message = "Hosts update", content = dataToSend, sha = sha.sha
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

@Serializable
data class Host(
    val ip: String, val name: String
)

@Serializable
data class GithubModel(
    val message: String, val content: String, val sha: String
)

@Serializable
data class GithubFileBlobResponse(
    val sha: String
)