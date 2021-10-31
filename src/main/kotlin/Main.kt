import com.github.lipen.satlib.core.Lit
import com.github.lipen.satlib.op.exactlyOne
import com.github.lipen.satlib.solver.GlucoseSolver
import com.github.lipen.satlib.solver.Solver


val edges = mutableListOf<List<Lit>>()

fun main() {
    val k = 2
    val solver = GlucoseSolver()
    solver.context

    var n = 2
    while (true) {
        println("START FOR N = $n")

        addClausesForVertex(solver, k, n)

        println("NUMBER OF CLAUSES = ${solver.numberOfClauses}")

        if (!solver.solve()) {
            println("ANSWER: ${n - 1}")
            break
        } else {
            println(solver.getModel())
            println("--- END ---")
        }

        n++
    }
}

fun addClausesForVertex(solver: Solver, colorCount: Int, vertexNumber: Int) {
    if (vertexNumber < 2) throw IllegalArgumentException("Номер вершины должен быть больше 2")

    // добавляем по ребру к каждой из существующих вершин
    val newEdgesCount = vertexNumber - 1
    repeat(newEdgesCount) {
        edges.add(List(colorCount) { solver.newLiteral() })
    }

    addUniqColorClauses(solver, vertexNumber)
    addEdgesAdjacencyClauses(solver, vertexNumber)
}

// накладываем ограничение, что ребро может быть окрашено только в один цвет
fun addUniqColorClauses(solver: Solver, vertexNumber: Int) {
    for (i in 1 until vertexNumber) {
        val edge = edges[edges.size - i]
        solver.exactlyOne(edge)
    }
}

fun addEdgesAdjacencyClauses(solver: Solver, vertexNumber: Int) {
    val vCount = vertexNumber - 1
    // перебираем пары вершин, с которыми можно составить треугольник для вершины vertexNumber
    for (i in 1..vCount) {
        for (j in (i + 1)..vCount) {
            val e1 = getEdgeByVertexes(i, j)
            val e2 = getEdgeByVertexes(i, vertexNumber)
            val e3 = getEdgeByVertexes(j, vertexNumber)

            // накладываем ограничение, что три смежных ребра не могут быть окрашены в один цвет
            for (k in e1.indices) {
                solver.addClause(-e1[k], -e2[k], -e3[k])
            }
        }
    }
}

// по номерам вершин получаем ребро между ними
fun getEdgeByVertexes(i: Int, j: Int): List<Lit> {
    if (i >= j) throw IllegalArgumentException("Чё за ерунда?")
    val offset = (j - 1) * (j - 2) / 2
    return edges[offset + (i - 1)]
}