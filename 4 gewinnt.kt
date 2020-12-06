@file:Suppress("SpellCheckingInspection", "NonAsciiCharacters")

package gew

import kotlin.streams.toList

var brett = mutableListOf(
        0, 0, 0, 0, 0, 0, 0,
        0, 0, 0, 0, 0, 0, 0,
        0, 0, 0, 0, 0, 0, 0,
        0, 0, 0, 0, 0, 0, 0,
        0, 0, 0, 0, 0, 0, 0,
        0, 0, 0, 0, 0, 0, 0
)

enum class Schwierigkeit(val minMax: Int, val monteCarlo: Int) {
    leicht(minMax = 1, monteCarlo = 20000),
    mittel(minMax = 2, monteCarlo = 10000),
    schwer(minMax = 3, monteCarlo = 5000)
}


fun menschZug(züge: Int) {
    println("Du bist am Zug ${
        if (züge < 2) {
            "(1 - 7)"
        } else ""
    } ")

    while (true) {
        val eingabe = readLine()?.toIntOrNull()
        if (eingabe == null || eingabe < 1 || eingabe > 7 || brett[eingabe - 1] != 0) {
            println("Falsche Eingabe")
            continue
        }
        if (zug(eingabe - 1, brett, -1)) {
            return
        }
        println("Falsche Eingabe")
    }
}

data class Knoten(val kinder: List<Knoten>, var wert: Int = 0, val brett: List<Int>, val zug: Int, val dran: Int)

fun generieren(brett: List<Int>, feld: Int, dran: Int, züge: Int, tabelle: MutableMap<List<Int>, Knoten>): Knoten {
    val alterKnoten = tabelle[brett]
    if (alterKnoten != null) {
        return alterKnoten
    }

    val kinder = mutableListOf<Knoten>()
    if (züge > 0) {
        möglichkeiten(brett).forEach { spalte ->
            val brett1 = brett.toMutableList()
            zug(spalte, brett1, dran)
            if (gewonnen(brett1, 1) || gewonnen(brett1, -1)) {
                kinder.add(Knoten(listOf(), brett = brett1, zug = spalte, dran = dran))
            } else {
                kinder.add(generieren(brett1, spalte, dran * -1, züge = züge - 1, tabelle))
            }
        }
    }

    val knoten = Knoten(kinder, brett = brett, zug = feld, dran = dran)
    tabelle[brett] = knoten
    return knoten
}

fun minMax(minMax: Int, knoten: Knoten, monteCarlo: Int) {
    if (gewonnen(knoten.brett, -1)) {
        knoten.wert = -monteCarlo - 1
        return
    } else if (gewonnen(knoten.brett, 1)) {
        knoten.wert = monteCarlo + 1
        return
    } else if (knoten.kinder.isEmpty()) {
        knoten.wert = monteCarlo(brett = knoten.brett, dran = knoten.dran * -1, knoten = knoten, monteCarlo)
    }
    knoten.kinder.forEach { kind -> minMax(minMax * -1, kind, monteCarlo) }

    if (knoten.kinder.isEmpty()) {
        return
    }

    if (minMax == 1) {
        knoten.wert = knoten.kinder.maxOf { kind -> kind.wert }
    } else {
        knoten.wert = knoten.kinder.minOf { kind -> kind.wert }
    }
}

fun monteCarlo(brett: List<Int>, dran: Int, knoten: Knoten, monteCarlo: Int): Int {
    var durchläufe = monteCarlo
    var wert = 0
    val züge = 0

    wert = (0..durchläufe).toList().parallelStream()
            .map { durchlauf(brett = brett.toMutableList(), dran = dran, knoten = knoten, wert = wert, züge = züge) }
            .toList().sum()
    return wert
}

fun durchlauf(brett: MutableList<Int>, dran: Int, knoten: Knoten, wert: Int, züge: Int): Int {

    var möglichkeiten = möglichkeiten(brett)
    if (möglichkeiten.isEmpty()) {
        return 0
    }

    var spalte = möglichkeiten.random()
    zug(spalte, brett, dran)
    if (gewonnen(brett, wer = -1)) {
        return -1
    }
    if (gewonnen(brett, wer = 1)) {
        return 1
    }


    return durchlauf(brett = brett, dran = dran * -1, knoten = knoten, wert = wert, züge = züge + 1)
}

private fun zug(spalte: Int, brett: MutableList<Int>, dran: Int): Boolean {
    var zeile = 5
    var index = spalte + 35
    while (zeile >= 0) {
        if (brett[index] == 0) {
            if (dran == 1) {
                brett[index] = 1
            } else {
                brett[index] = -1

            }
            return true
        } else {
            index -= 7
            zeile -= 1
        }
    }
    return false
}

private fun möglichkeiten(brett: List<Int>): MutableList<Int> {
    var möglichkeiten = mutableListOf<Int>()
    var spalte: Int = 0
    while (spalte < 7) {
        if (brett[spalte] == 0) {
            möglichkeiten.add(spalte)
        }
        spalte += 1
    }
    return möglichkeiten
}

fun computerZug(s: Schwierigkeit) {
    val start = System.currentTimeMillis()
    if (s == Schwierigkeit.schwer) {
        println("Bitte warten...")
    }
    var k = generieren(brett.toMutableList(), -1, 1, züge = s.minMax, mutableMapOf())
    minMax(1, k, s.monteCarlo)
    val max = k.kinder.maxOf { kind -> kind.wert }
    val liste = mutableListOf<Knoten>()
    k.kinder.forEach { kind ->
        if (kind.wert == max) {
            liste.add(kind)
        }
    }
    val zug = liste.random().zug
    println("wert = $max, zug = $zug dauer = ${(System.currentTimeMillis() - start) / 1000}")
    zug(zug, brett, 1)
}

fun gewonnen(brett: List<Int>, wer: Int): Boolean {
    var zeile = 0
    while (zeile < 6) {
        var spalte = 0
        while (spalte < 4) {
            val index = 7 * zeile + spalte
            if (brett[index] == wer && brett[index + 1] == wer && brett[index + 2] == wer && brett[index + 3] == wer) {
                return true
            } else {
                spalte += 1
            }
        }

        zeile += 1
    }
    zeile = 0
    while (zeile < 3) {
        var spalte = 0
        while (spalte < 7) {
            val index = 7 * zeile + spalte
            if (brett[index] == wer && brett[index + 7] == wer && brett[index + 14] == wer && brett[index + 21] == wer) {
                return true
            } else {
                spalte += 1
            }
        }

        zeile += 1
    }
    zeile = 0
    while (zeile < 3) {
        var spalte = 0
        while (spalte < 4) {
            val index = 7 * zeile + spalte
            if (brett[index] == wer && brett[index + 8] == wer && brett[index + 16] == wer && brett[index + 24] == wer) {
                return true
            } else {
                spalte += 1
            }
        }

        zeile += 1
    }
    zeile = 0
    while (zeile < 3) {
        var spalte = 3
        while (spalte < 7) {
            val index = 7 * zeile + spalte
            if (brett[index] == wer && brett[index + 6] == wer && brett[index + 12] == wer && brett[index + 18] == wer) {
                return true
            } else {
                spalte += 1
            }
        }

        zeile += 1
    }
    return false
}

fun start(): Int {
    while (true) {
        println("Wills du beginnen (j/n)")
        val e = readLine()
        if (e == "j") {
            return 1
        } else if (e == "n") {
            return -1
        }
        println("Falsche Eingabe")
    }

}

fun main() {
    System.setProperty("java.util.concurrent.ForkJoinPool.common.parallelism", "8")
    //brett = leseBrett().toMutableList()

    val s = schwierigkeit()
    var dran = start()
    var runde = 0
    while (true) {
        if (dran == -1) {
            computerZug(s)
            dran = 1
        } else {
            menschZug(züge = runde)
            dran = -1
        }
        println("+---------------+")
        var zeile = 0
        while (zeile < 6) {
            var spalte = 0
            print("| ")
            while (spalte < 7) {
                print(when (brett[zeile * 7 + spalte]) {
                    0 -> "  "
                    1 -> "o "
                    else -> "x "
                })

                spalte += 1

            }

            println("|")
            zeile += 1
        }
        println("+---------------+")

        if (gewonnen(brett, 1)) {
            println("Du hast verloren")
            return
        }
        if (gewonnen(brett, -1)) {
            println("Du hast gewonnen")
            return
        }
        runde += 1
        if (runde == 42) {
            println("Es ist unentschieden")
            return
        }
    }
}

fun schwierigkeit(): Schwierigkeit {
    val size = Schwierigkeit.values().size
    println("Wähle die Schwierigkeit (1 - $size)")
    while (true) {
        val eingabe = readLine()?.toIntOrNull()

        if (eingabe == null || eingabe < 1 || eingabe > size) {
            println("Falsche Eingabe")
            continue
        }
        return Schwierigkeit.values()[eingabe - 1]
    }
}

fun leseBrett(): List<Int> {
    val s = """
    +---------------+
    |   x o x o x   |
    |   x x o o x   |
    | x o o o x x   |
    | o x x o o o   |
    | o x o x x x   |
    | o x o o x o   |
    +---------------+
            """
    return s.split("\n")
            .drop(2)
            .take(6)
            .map { zeile ->
                val ohne = zeile.substringAfter("|")
                (0..6).map { spalte ->
                    when (ohne[spalte * 2 + 1]) {
                        'x' -> -1
                        'o' -> 1
                        else -> 0
                    }
                }
            }
            .flatten()
}