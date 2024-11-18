PushHB Roadmap - Last updated II 16th Nov 2024

```mermaid
gantt
    title PushKB Roadmap
    dateFormat  YYYY-MM-DD
    excludes    weekends

    section Development
    Make Release R1                  :t3-0, 2024-11-18, 9d
    R1                               :milestone, after t3-0, 0d
    Valut                            :t3-1, after t3-0, 5d
    Scaling                          :t3-2, after t3-1, 2d
    Keycloak                         :t3-3, after t3-2, 1d
    API                              :t3-4, after t3-2, 1d
		V1                               :milestone, after t3-4, 0d

    section Devops
    Install R1                       :t4-1, after t3-0, 5d

    section Testing
    QA R1                            :t5-1, after t4-1, 10d

```

