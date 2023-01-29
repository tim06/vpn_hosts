/*fun parseFromCsv(url: String, token: String) = runBlocking {
    val reader = URL(url).openStream().bufferedReader()
    reader.readLine()
    reader.readLine()
    val result = reader.lineSequence().filter { it.isNotBlank() }.mapNotNull {
        if (it.contains('*')) {
            null
        } else {
            println(it)
            val splited = it.split(',', ignoreCase = false, limit = 15)
            val hostName = splited.get(0)
            val ip = splited.get(1)
            val score = splited.get(2)
            val ping = splited.get(3)
            val speed = splited.get(4)
            val countryLong = splited.get(5)
            val countryShort = splited.get(6)
            val numVpnSessions = splited.get(7)
            val uptime = splited.get(8)
            val totalUsers = splited.get(9)
            val totalTraffic = splited.get(10)
            val logType = splited.get(11)
            val operator = splited.get(12)
            val message = splited.get(13)
            val openvpnConfigDataBase64 = splited.get(14)
            CSVModel(
                hostName,
                ip,
                score,
                ping,
                speed,
                countryLong,
                countryShort,
                numVpnSessions,
                uptime,
                totalUsers,
                totalTraffic,
                logType,
                operator,
                message,
                openvpnConfigDataBase64
            )
        }
    }.toList()
    sendHosts(sendHosts, token)
}*/

data class CSVModel(
    val hostName: String,
    val ip: String,
    val score: String,
    val ping: String,
    val speed: String,
    val countryLong: String,
    val countryShort: String,
    val numVpnSessions: String,
    val uptime: String,
    val totalUsers: String,
    val totalTraffic: String,
    val logType: String,
    val operator: String,
    val message: String,
    val openvpnConfigDataBase64: String
)