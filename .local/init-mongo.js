db.createUser(
  {
    user: "harmny",
    pwd: "harmny",
    roles: [
      {
        role: "readWrite",
        db: "harmny"
      }
    ]
  }
)