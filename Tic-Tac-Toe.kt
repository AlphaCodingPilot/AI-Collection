val brett = mutableListOf(
        0, 0, 0, 0, 0, 0, 0, 0, 0
)

fun menschZug(): Int {
    println("Du bist am Zug")
    while (true) {
        val eingabe = readLine()?.toIntOrNull()
        if (eingabe != null && eingabe > 0 && eingabe < 10) {
            if (brett[eingabe - 1] == 0) {
                brett[eingabe - 1] = -1
                return eingabe - 1
            }
        }
        println("Falsche Eingabe")
    }

}

data class Knoten(val kinder: List<Knoten>, var wert: Int = 0, val brett: List<Int>, val zug: Int)

fun generieren(brett: List<Int>, feld: Int, dran: Int): Knoten {
    val kinder = mutableListOf<Knoten>()
    brett.forEachIndexed { feld, _ ->
        if (brett[feld] == 0) {
            val brett1 = brett.toMutableList()
            brett1[feld] = dran
            if (gewonnen(brett1, 1) || gewonnen(brett1, -1)) {
                kinder.add(Knoten(listOf(), brett = brett1, zug = feld))
            } else {
                kinder.add(generieren(brett1, feld, dran * -1))
            }
        }
    }
    return Knoten(kinder, brett = brett, zug = feld)
}

fun minMax(minMax: Int, knoten: Knoten) {
    if (gewonnen(knoten.brett, -1)) {
        knoten.wert = -1
        return
    }
    if (gewonnen(knoten.brett, 1)) {
        knoten.wert = 1
        return
    }
    knoten.kinder.forEach { kind -> minMax(minMax * -1, kind) }

    if (knoten.kinder.isEmpty()) {
        return
    }

    if (minMax == 1) {
        knoten.wert = knoten.kinder.maxOf { kind -> kind.wert }
    } else {
        knoten.wert = knoten.kinder.minOf { kind -> kind.wert }
    }
}

fun computerZug(knoten: Knoten): Int {
    val max = knoten.kinder.maxOf { kind -> kind.wert }
    val liste = mutableListOf<Knoten>()
    knoten.kinder.forEach { kind ->
        if (kind.wert == max) {
            liste.add(kind)
        }
    }
    val zug = liste.random().zug
    brett[zug] = 1
    return zug
}

fun gewonnen(brett: List<Int>, wer: Int): Boolean {
    return (brett[0] == wer && brett[1] == wer && brett[2] == wer ||
            brett[3] == wer && brett[4] == wer && brett[5] == wer ||
            brett[6] == wer && brett[7] == wer && brett[8] == wer ||
            brett[1] == wer && brett[4] == wer && brett[7] == wer ||
            brett[2] == wer && brett[5] == wer && brett[8] == wer ||
            brett[0] == wer && brett[4] == wer && brett[8] == wer ||
            brett[2] == wer && brett[4] == wer && brett[6] == wer ||
            brett[0] == wer && brett[3] == wer && brett[6] == wer)
}

fun start(): Int {
    while (true) {
        println("Wills du beginnen (j/n)")
        val e = readLine()
        if (e == "j") {
            return -1
        } else if (e == "n") {
            return 1
        }
        println("Falsche Eingabe")
    }

}

fun main() {

    var dran = start()
    var k = generieren(brett.toMutableList(), -1, dran)
    minMax(dran, k)
    var runde = 0
    while (true) {
        val zug = if (dran == -1) {
            val z = menschZug()
            dran = 1
            z
        } else {
            val z = computerZug(k)
            dran = -1
            z
        }

        println("+---------+")
        println(
                "| ${if (brett[0] == -1) "x" else if (brett[0] == 0) " " else "o"}  " +
                        "${if (brett[1] == -1) "x" else if (brett[1] == 0) " " else "o"}  " +
                        "${if (brett[2] == -1) "x" else if (brett[2] == 0) " " else "o"} |"
        )
        println(
                "| ${if (brett[3] == -1) "x" else if (brett[3] == 0) " " else "o"}  " +
                        "${if (brett[4] == -1) "x" else if (brett[4] == 0) " " else "o"}  " +
                        "${if (brett[5] == -1) "x" else if (brett[5] == 0) " " else "o"} |"
        )
        println(
                "| ${if (brett[6] == -1) "x" else if (brett[6] == 0) " " else "o"}  " +
                        "${if (brett[7] == -1) "x" else if (brett[7] == 0) " " else "o"}  " +
                        "${if (brett[8] == -1) "x" else if (brett[8] == 0) " " else "o"} |"
        )
        println("+---------+")
        if (gewonnen(brett, 1)) {
            println("Du hast verloren")
            return
        }
        if (gewonnen(brett, -1)) {
            println("Du hast gewonnen")
            return
        }
        runde += 1
        if (runde == 9) {
            println("Es ist unentschieden")
            return
        }
        k.kinder.forEach { kind ->
            if (zug == kind.zug) {
                k = kind
            }
        }
    }
}