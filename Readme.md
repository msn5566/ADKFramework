graph TD
    subgraph "1. Setup & Initialization"
        A[User provides SRS file path] --> B{Read SRS File};
        B --> C{Parse Git Configuration};
        C --> D{Clone or Update Git Repository};
    end

    subgraph "2. Change Analysis"
        D --> E{Compare new SRS with previous version};
        E --> F{"Any functional<br/>changes?"};
        F -- "No" --> G[Exit Gracefully];
    end

    subgraph "3. AI Code Generation"
        F -- "Yes" --> H[Create New Feature Branch];
        H --> I[Run Main AI Workflow];
        I --> J("Requirements Agent<br/>(Summarizes work & creates commit message)");
        J --> K("Dependency Agent<br/>(Finds pom.xml dependencies)");
        K --> L("Code Generation Agent<br/>(Writes Spring Boot source code)");
        L --> M("Test Generation Agent<br/>(Writes JUnit tests)");
        M --> N[Collect All AI Outputs];
    end

    subgraph "4. Project Assembly & Submission"
        N --> O{Generate Project Files};
        O --> P["Java Source Files"];
        O --> Q["pom.xml"];
        O --> R["README.md"];
        O --> S["application.yml"];
        O --> T["GitHub Actions CI Config"];
        O --> U["AI_CHANGELOG.md"];
        U --> V{Commit & Push to Feature Branch};
        V --> W{Create GitHub Pull Request};
        W --> X[Open PR in Browser];
    end

    classDef exit fill:#f9f,stroke:#333,stroke-width:2px;
    class G exit;
    classDef success fill:#ccf,stroke:#333,stroke-width:2px;
    class X success;