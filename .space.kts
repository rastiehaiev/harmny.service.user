job("Publish to Docker Hub") {
    host("Build artifacts and a Docker image") {
        startOn {
            gitPush {
                branchFilter {
                    +"refs/heads/main"
                }
            }
        }

        // assign project secrets to environment variables
        env["SPACE_USER"] = Secrets("space_user")
        env["SPACE_TOKEN"] = Secrets("space_token")
        env["JWT_TOKEN"] = Secrets("jwt_token")

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
