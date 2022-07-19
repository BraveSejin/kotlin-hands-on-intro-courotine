package tasks

import contributors.*
import kotlinx.coroutines.*
import kotlin.coroutines.coroutineContext

suspend fun loadContributorsNotCancellable(service: GitHubService, req: RequestData): List<User> {
    val repos =
        service
            .getOrgReposCall(req.org)
            .also { logRepos(req, it) }
            .body() ?: listOf()

    lateinit var result : List<User>
    val deffereds = repos.map { repo ->

        GlobalScope.async {
            log("starting loading for ${repo.name}")
            delay(3000)
            service.getRepoContributorsCall(req.org, repo.name)
                .also { logUsers(repo, it) }
                .bodyList()
        }
    }

    result = deffereds.awaitAll().flatten().aggregate()
    return result

}