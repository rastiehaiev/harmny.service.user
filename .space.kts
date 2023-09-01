job("Build & Publish to Docker Registry") {
    startOn {
        gitPush {
            anyBranchMatching {
                +"refs/heads/main"
            }
        }
    }
    host("Build & Push Docker image") {

        // assign project secrets to environment variables
        env["SPACE_USER"] = "{{ project:space_user }}"
        env["SPACE_TOKEN"] = "{{ project:space_token }}"

        shellScript {
            content = """
                docker login harmony.registry.jetbrains.space --username ${'$'}SPACE_USER --password "${'$'}SPACE_TOKEN"
            """
        }

        dockerBuildPush {
            tags {
                +"harmony.registry.jetbrains.space/p/harmny/containers/harmny.service.user:latest"
            }
        }
    }
}
