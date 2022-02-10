import java.io.File

//Default width defined by the Puzzle
const val FIELD_WIDTH = 16
//Default height defined by the Puzzle
const val FIELD_HEIGHT = 16
//Possible chars in the puzzle
const val vFence = '|'
const val hFence = '-'
const val cornerFence = '+'
const val groundOutside = '.'
const val hole = 'o'
const val holeInGarden = 'O'
const val markedOutside = 'X'

/**
 * Reads a given file to a list
 *
 * @return List containing all lines of the map
 */
fun readInput(fileName: String): MutableList<String>  = File(fileName).useLines { it.toList() } as MutableList<String>

/**
 * Helper function to replace chars in a given string
 *
 * @param string String which will get a replacement
 * @param char Char to replace in the string
 * @param index Index of the replacement
 * @return String with a replaced char at the given index
 */
fun replaceChar(string: String, char: Char, index: Int): String {
    var replacedString = string.toCharArray()
    replacedString[index] = char
    return String(replacedString)
}

/**
 * Adapted flood fill implementation to mark fields out of the garden with a special symbol
 *
 * Uses the recursion based 4 way version:
 * https://en.wikipedia.org/wiki/Flood_fill#Stack-based_recursive_implementation_(four-way)
 *
 * @param y y-Coordinate of the current node
 * @param x x-Coordinate of the current node
 * @param mapOfGarden map of the garden
 */
fun floodFill(y: Int, x: Int, mapOfGarden: MutableList<String>) {
    //Perform out of bounds check
    if(y < 0 || y > FIELD_HEIGHT + 1 || x < 0 || x > FIELD_WIDTH + 1){
        return
    }
    //Don't fill in fences or already visited points
    if(mapOfGarden[y][x] == markedOutside || mapOfGarden[y][x] == vFence || mapOfGarden[y][x] == hFence || mapOfGarden[y][x] == cornerFence){
        return
    }
    mapOfGarden[y] = replaceChar(mapOfGarden[y], markedOutside, x)

    floodFill(y - 1 , x, mapOfGarden)
    floodFill(y + 1 , x, mapOfGarden)
    floodFill(y, x - 1, mapOfGarden)
    floodFill(y, x + 1, mapOfGarden)
}

/**
 * Check if a given node is located in a garden.
 *
 * After performing a floodfill it is easier to determine if a node is inside
 * a garden. Vertical fences immediately flip the bool, corner fences that were previously outside
 * will indicate a beginning of a garden. If an outside marker is found set the boolean to false.
 *
 * @param y y-Coordinate of the node
 * @param x x-Coordinate of the node
 * @param mapOfGarden List containing the puzzle map
 * @return true if the given node is in the garden, false otherwise
 */
fun isInGarden(y: Int, x: Int, mapOfGarden: MutableList<String>): Boolean {
    var isInside = false
    val lineOfHole = mapOfGarden[y].substring(0, x) //Not needed to get the whole line, just until the mole hole

    for ((c) in lineOfHole.withIndex()){

        if(lineOfHole[c] == markedOutside){
            isInside = false
        }

        if (lineOfHole[c] == vFence) {
            isInside = !isInside
        }

        if(lineOfHole[c] == cornerFence){
            if(!isInside){
                isInside = true
            }
        }
    }
    return isInside
}

/**
 * Set up the map, appends extra fields and calls the floodfill/verification logic
 *
 * @param args contains the path to the puzzle input
 */
fun main(args: Array<String>) {
    val mapOfGarden = readInput(args[0]).toMutableList()
    //Add extra ring of '.' around the map to ensure having a starting point
    mapOfGarden.add(0, groundOutside.toString().repeat(FIELD_WIDTH))
    mapOfGarden.add(groundOutside.toString().repeat(FIELD_WIDTH))
    for((i) in mapOfGarden.withIndex()){
        mapOfGarden[i].plus('.')
        val line = mapOfGarden[i]
        mapOfGarden[i] = ".$line."
    }

    //Use all '.' (ground outside) occurrences as starting point of the flood fill
    //Since we expanded the given map with a ring of '.''s it is guaranteed to have a starting point.
    while(mapOfGarden.toString().count { c -> c == groundOutside } > 1){
        for ((y) in mapOfGarden.withIndex()){
            for((x) in mapOfGarden.withIndex()){
                if(mapOfGarden[y][x] == groundOutside){
                    floodFill(y,x, mapOfGarden)
                }
            }
        }
    }

    //Since the flood fill only captures regions that have a '.' as starting point there is a possibility to miss
    //regions enclosed in gardens. Go through the remaining
    //If there are no holes, skip this part
    if(mapOfGarden.toString().count { c -> c == hole } > 0) {
        for ((y) in mapOfGarden.withIndex()) {
            for ((x) in mapOfGarden.withIndex()) {
                if (mapOfGarden[y][x] == hole) {
                    if (isInGarden(y, x, mapOfGarden)) {
                        mapOfGarden[y] = replaceChar(mapOfGarden[y], holeInGarden, x)
                    } else {
                        mapOfGarden[y] = replaceChar(mapOfGarden[y], markedOutside, x)
                    }
                }
            }
        }
    }
    //mapOfGarden.forEach{ println(it) } // Coding game doesn't like extra output, can be commented out to get an overview of the state of the field
    println(mapOfGarden.toString().count { c -> c == holeInGarden })
}

