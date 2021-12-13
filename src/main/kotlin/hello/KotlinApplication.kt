package hello

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean
import org.springframework.http.MediaType.APPLICATION_JSON
import org.springframework.web.reactive.function.server.ServerResponse
import org.springframework.web.reactive.function.server.body
import org.springframework.web.reactive.function.server.router
import reactor.core.publisher.Mono

@SpringBootApplication
class KotlinApplication {

    @Bean
    fun routes() = router {
        GET {
            ServerResponse.ok().body(Mono.just("Let the battle begin!"))
        }

        POST("/**", accept(APPLICATION_JSON)) { request ->
            request.bodyToMono(ArenaUpdate::class.java).flatMap { arenaUpdate ->
                println(arenaUpdate)
                ServerResponse.ok().body(Mono.fromCallable{
                    strategy(arenaUpdate)
                    })
            }
        }
    }
}

fun main(args: Array<String>) {
    runApplication<KotlinApplication>(*args)
}

fun strategy(arenaUpdate: ArenaUpdate):String {
    val myState = findMe(arenaUpdate)
    if (myState == null) {
        return "T"
    }
    else if (myState.wasHit){
        return runAwayFromHit(arenaUpdate, myState)
    }
    else {
        if (enemyInFront(arenaUpdate, myState)) return "T"
        if (enemyOnRight(arenaUpdate, myState)) return "R"
        if (enemyOnLeft(arenaUpdate, myState)) return "L"
        return if (isOnEdge(arenaUpdate, myState)) "R" else "F"
    }
}

fun findMe(arenaUpdate: ArenaUpdate):PlayerState? {
    return arenaUpdate.arena.state.get("https://34.149.205.15.sslip.io/")
}

fun enemyInFront(arenaUpdate: ArenaUpdate, myState: PlayerState): Boolean{
    if (myState.direction == "N"){
        if (arenaUpdate.arena.state.filterValues { it.y == myState.y-1 && it.x == myState.x}.isNotEmpty()) return true
    }

    if (myState.direction == "E"){
        if (arenaUpdate.arena.state.filterValues { it.x == myState.x+1 && it.y == myState.y}.isNotEmpty()) return true
    }

    if (myState.direction == "S"){
        if (arenaUpdate.arena.state.filterValues { it.y == myState.y+1 && it.x == myState.x}.isNotEmpty()) return true
    }

    if (myState.direction == "W"){
        if (arenaUpdate.arena.state.filterValues { it.x == myState.x-1 && it.y == myState.y}.isNotEmpty()) return true
    }
    return false
}

fun enemyOnRight(arenaUpdate: ArenaUpdate, myState: PlayerState): Boolean{
    if (myState.direction == "E"){
        if (arenaUpdate.arena.state.filterValues { it.y == myState.y-1 && it.x == myState.x}.isNotEmpty()) return true
    }

    if (myState.direction == "N"){
        if (arenaUpdate.arena.state.filterValues { it.x == myState.x+1 && it.y == myState.y}.isNotEmpty()) return true
    }

    if (myState.direction == "W"){
        if (arenaUpdate.arena.state.filterValues { it.y == myState.y+1 && it.x == myState.x}.isNotEmpty()) return true
    }

    if (myState.direction == "S"){
        if (arenaUpdate.arena.state.filterValues { it.x == myState.x-1 && it.y == myState.y}.isNotEmpty()) return true
    }
    return false
}

fun enemyOnLeft(arenaUpdate: ArenaUpdate, myState: PlayerState): Boolean{
    if (myState.direction == "W"){
        if (arenaUpdate.arena.state.filterValues { it.y == myState.y-1 && it.x == myState.x}.isNotEmpty()) return true
    }

    if (myState.direction == "S"){
        if (arenaUpdate.arena.state.filterValues { it.x == myState.x+1 && it.y == myState.y}.isNotEmpty()) return true
    }

    if (myState.direction == "E"){
        if (arenaUpdate.arena.state.filterValues { it.y == myState.y+1 && it.x == myState.x}.isNotEmpty()) return true
    }

    if (myState.direction == "N"){
        if (arenaUpdate.arena.state.filterValues { it.x == myState.x-1 && it.y == myState.y}.isNotEmpty()) return true
    }
    return false
}

fun isOnEdge(arenaUpdate: ArenaUpdate, myState: PlayerState): Boolean{
    return (myState.x == 0 && myState.direction == "W")||
            (myState.x >= (arenaUpdate.arena.dims[0] - 1) && myState.direction == "E") ||
            (myState.y == 0 && myState.direction == "N") ||
(            myState.y >= (arenaUpdate.arena.dims[1] - 1) && myState.direction == "S")
}

fun runAwayFromHit(arenaUpdate: ArenaUpdate, myState: PlayerState): String{
    return if (!enemyInFront(arenaUpdate, myState)){
        "W"
    } else if (!enemyOnRight(arenaUpdate, myState)){
        "R"
    } else if (!enemyOnLeft((arenaUpdate, myState)){
        "L"
    } else "T"
}


data class ArenaUpdate(val _links: Links, val arena: Arena)
data class PlayerState(val x: Int, val y: Int, val direction: String, val score: Int, val wasHit: Boolean)
data class Links(val self: Self)
data class Self(val href: String)
data class Arena(val dims: List<Int>, val state: Map<String, PlayerState>)