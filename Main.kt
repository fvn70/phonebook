package phonebook

import java.io.File
import kotlin.math.floor
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sqrt

var lineTime = 0L

fun main() {
    val dir = File("C:\\Data\\JBA\\directory.txt").readLines()
    val find = File("C:\\Data\\JBA\\find.txt").readLines()

    sortAndFind(find, dir, "", "line", "linear search")
    sortAndFind(find, dir, "bubble", "jump", "bubble sort + jump search")
    sortAndFind(find, dir, "quick", "binary", "quick sort + binary search")
    sortAndFind(find, dir, "hash", "hash", "hash table")
}

fun MutableList<String>.createHash(): HashMap<String, String> {
    val hmap = hashMapOf<String, String>()
    this.map { hmap.put(it.substringAfter(" "), it) }
    return hmap
}

fun <T : Comparable<T>> MutableList<T>.findByJump(item: T): Int {
    val blockSize = floor(sqrt(this.size.toDouble())).toInt()
    var i = 0
    while (this[i].toString().substringAfter(" ") < item.toString()) {
        i = min(i + blockSize, this.lastIndex)
        if (i == this.lastIndex) break
    }
    val imax = max(0, i - blockSize)
    while (i > imax) {
        if (this[i].toString().substringAfter(" ") == item) return i
        i--
    }
    return -1
}

fun findByLine(item: String, list: List<String>): Int {
    for (i in 0..list.lastIndex)
        if (list[i].substringAfter(" ") == item) return i
    return -1
}

fun <T : Comparable<T>> MutableList<T>.findBinary(item: T, low: Int, high: Int): Int {
    if (low > high) return -1
    val size = high - low + 1
    val m = low + size / 2
    return when {
        this[m].toString().substringAfter(" ") == item.toString() -> m
        this[m].toString().substringAfter(" ") > item.toString() -> findBinary(item, low, m - 1)
        else -> findBinary(item, m + 1, high)
    }
}

fun <T : Comparable<T>> MutableList<T>.sortQuick(low: Int, high: Int) {
    if (low < high) {
        val p = this.partition(low, high)
        this.sortQuick(low, p - 1)
        this.sortQuick(p + 1, high)
    }
}

fun <T> MutableList<T>.swapAt(first: Int, second: Int) {
    val tmp = this[second]
    this[second] = this[first]
    this[first] = tmp
}

fun <T : Comparable<T>> MutableList<T>.partition(low: Int, high: Int): Int {
    val p = this[high].toString().substringAfter(" ")
    var i = low
    for (j in low until high) {
        if (this[j].toString().substringAfter(" ") <= p) {
            swapAt(i, j)
            i++
        }
    }
    swapAt(i, high)
    return i
}
// return false if work time > maxTime
fun <T : Comparable<T>> MutableList<T>.sortByBuble(maxTime: Long): Boolean {
    val start = System.currentTimeMillis()
    var time = start
    for (i in this.lastIndex downTo 1)
        for (j in 0..i - 1) {
            time = System.currentTimeMillis() - start
            if (time > maxTime) {
                return false
            }
            if (this[j].toString().substringAfter(" ") > this[j+1].toString().substringAfter(" ")) {
                this.swapAt(j, j + 1)
            }
        }
    return true
}

fun sortAndFind(
        find: List<String>,
        dir: List<String>,
        sortType: String,
        searchType: String,
        title: String
) {

    println("Start searching ($title)...")
    var start = System.currentTimeMillis()
    val sortDir = dir.toMutableList()
    var hashDir = hashMapOf<String, String>()
    var stop = false
    when (sortType) {
        "bubble" -> {
            stop = !sortDir.sortByBuble(lineTime * 10)
        }
        "quick" -> {
            sortDir.sortQuick(0, sortDir.lastIndex)
        }
        "hash" -> {
            hashDir = sortDir.createHash()
        }
    }
    val sortTime = System.currentTimeMillis() - start
    start = System.currentTimeMillis()
    var cnt = 0
    when (searchType) {
        "line" -> {
            for (w in find) if (findByLine(w, dir) > -1) cnt++
            lineTime = System.currentTimeMillis() - start

        }
        "jump" -> {
            if (stop) {
                for (w in find) if (findByLine(w, dir) > -1) cnt++
            } else {
                for (w in find) if (sortDir.findByJump(w) > -1) cnt++
            }
        }
        "binary" -> {
            for (w in find) if (sortDir.findBinary(w, 0, sortDir.lastIndex) > -1) cnt++
        }
        "hash" -> {
            for (w in find) if (hashDir.containsKey(w)) cnt++
        }
    }

    val findTime = System.currentTimeMillis() - start
    var time = String.format("%1\$tM min. %1\$tS sec. %1\$tL ms.", sortTime + findTime)
    println("Found $cnt / ${find.size} entries. Time taken: $time")

    if (searchType != "line") {
        time = String.format("%1\$tM min. %1\$tS sec. %1\$tL ms.", sortTime)
        val txt = if (searchType == "hash") "Creating" else "Sorting"
        println("$txt time: $time. ")
        if (stop)
            println("- STOPPED, moved to linear search")
        time = String.format("%1\$tM min. %1\$tS sec. %1\$tL ms.", findTime)
        println("Searching time: $time.")
    }
    println()
}
