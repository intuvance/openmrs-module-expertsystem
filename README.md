
# A.I Expert System
 
![Logo](https://github.com/user-attachments/assets/c5e1409d-8fb8-4016-9ef6-855172c8e2d1)

An `OpenMRS module` that uses `Artificial Intelligence` **( A.I )** to mimic the judgment and problem-solving abilities of a human expert in the use of digital tools, and electronic platforms to enhance healthcare delivery, improve patient care, and ultimately improve health outcomes.

Clinicians will query the EMR for *population-level* health questions by typing their questions ( e.g. “What percentage of my patients have diabetes?”) into a frontend built-in search bar. 

The query will be sent through this module to locally deployed `LLMs` and the results will show, explaining the derived answer. Citations and the SQL used will be included, so that surprising findings can be confirmed.

This will allow clinicians to leverage `LLMs` natural language capabilities in the world’s most broadly implemented open-source EMR `(OpenMRS)` to rapidly access, collate, and summarize acutely relevant *population-level* data.

## License

This Source Code is subject to the terms of the [Mozilla Public License, v. 2.0.](http://mozilla.org/MPL/2.0/)

[![License: MPL 2.0](https://img.shields.io/badge/License-MPL%202.0-brightgreen.svg)](https://opensource.org/licenses/MPL-2.0)

However, in addition to the full license included here, each file should contain the text found in license-header.txt at the start of the file.

For Java files, we should have the following message:

```java
/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 * 
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
```

For XML files, we should use the following:

```xml
<!--
    This Source Code Form is subject to the terms of the Mozilla Public License,
    v. 2.0. If a copy of the MPL was not distributed with this file, You can
    obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
    the terms of the Healthcare Disclaimer located at http://openmrs.org/license.

    Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
    graphic logo is a trademark of OpenMRS Inc.
-->
```

For properties files, we should use the following:

```properties
#
# This Source Code Form is subject to the terms of the Mozilla Public License,
# v. 2.0. If a copy of the MPL was not distributed with this file, You can
# obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
# the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
#
# Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
# graphic logo is a trademark of OpenMRS Inc.
#
```

Check whether all the supported files have the license header text. 

```
mvn license:check
```

In any case, the appropriate license header can easily be added to any existing file that needs it by running:

```
mvn license:format
```

#### Contributions

![GitHub contributors](https://img.shields.io/github/contributors/miirochristopher/openmrs-module-expertsystem) ![GitHub commit activity](https://img.shields.io/github/commit-activity/m/miirochristopher/openmrs-module-expertsystem)

#### Issues

![GitHub Issues or Pull Requests](https://img.shields.io/github/issues/miirochristopher/openmrs-module-expertsystem)

#### Downloads

![GitHub Release](https://img.shields.io/github/v/release/miirochristopher/openmrs-module-expertsystem)
![GitHub Downloads (all assets, all releases)](https://img.shields.io/github/downloads/miirochristopher/openmrs-module-expertsystem/total)

#### Coverage

![Codecov](https://img.shields.io/codecov/c/github/miirochristopher/openmrs-module-expertsystem)

### Authors

- [@miirochristopher](https://github.com/miirochristopher)

## Technology Stack

This module uses tools and utilities common to the `OpenMRS` environment, such as the `Spring Framework`, `Hibernate`, `Liquibase`, `Slf4j`, `JUnit`, and `Mockito`. `A.I Expert System` includes only [LangChain4j](https://docs.langchain4j.dev), [Testcontainers](https://java.testcontainers.org/) and [Lombok](https://projectlombok.org/) as additional libraries to the default libraries to limit chances of interference with other modules and avoid having to write so much repetitive code.

We also follow up-to-date usage methods with these libraries. For example, we use annotation-based configuration for everything wherever possible rather than their XML equivalents.

### External dependencies

This module depends on [Ollama](https://ollama.com/) which is an `open-source` tool that allows users to run large language models `(LLMs)` locally on their computers.

It simplifies the process of downloading, managing, and executing these models, providing a command-line interface and an `API` for interaction. Ollama enables users to work with `LLMs` without relying on cloud services, offering greater control over data privacy and performance. 

You can [download](https://ollama.com/download), install and configure Ollama for your respective operating system. Alternatively, if you have [Docker](https://www.docker.com/) and [Docker Compose](https://docs.docker.com/compose/) installed on your system, follow the Ollama setup instructions in this companion [repository](https://github.com/miirochristopher/ollama-expertsystem)'s `README`. 

### Benefits of using Ollama:
1. **Privacy and Control:** Users maintain full control over their data by running models locally. 
2. **Reduced Latency:** Local execution can lead to faster response times compared to cloud-based solutions. 
3. **Offline Access:** Ollama allows users to work with `LLMs` even without an internet connection. 
4. **Customization:** Users can customize models through *Modelfiles*, tailoring them to specific needs. 
5. **Accessibility:** Ollama makes powerful `LLMs` accessible to a wider audience, including those with limited technical expertise. 


## Unit & Integration Testing | Testing LLM Responses

While `LLMs` are powerful, they’re prone to generating hallucinations, and their responses may not always be relevant, appropriate, or factually accurate.

**One solution for evaluating LLM responses is to use an LLM itself**, preferably a separate one. `LangChain4j` provides evaluators to assess the quality of responses generated by Large Language Models `(LLMs)` within an application.

Most of this modules' code is tested with automated unit and integration tests. In addition to adhering to the recommended testing standards, carefully selected and trusted/proved tooling is used.

[Testcontainers](https://java.testcontainers.org/) provides us with several advantages for automated integration testing. 

    1. It offers realistic testing environments. 
    2. Isolation between tests, and easy cleanup. 

This ultimately leads to more reliable, reproducible, and efficient testing workflows which when combined with good practice such as using Hamcrest's [assertThat()](https://hamcrest.org/JavaHamcrest/javadoc/2.2/org/hamcrest/MatcherAssert.html#assertThat-T-org.hamcrest.Matcher-) method for more "readable" assertions can guarantee the much-needed reduced development costs, easier maintenance, improved team collaboration, and enhanced reliability.
These evaluators can leverage other `LLMs` (acting as `"judge"` `LLMs`) to assess the quality of the primary `LLM's` responses against the defined criteria. For instance, an evaluator might compare the generated response to the expected answer, analyze its semantic similarity, or check for specific characteristics like factual accuracy.

We use [Testcontainers](https://java.testcontainers.org/) to set up the Ollama service for our tests, the prerequisite for which is an active [Docker](https://www.docker.com/) instance. 

We import the [Ollama Testcontainers](https://central.sonatype.com/artifact/org.testcontainers/ollama) dependency for Spring Boot and the [Ollama](https://mvnrepository.com/artifact/org.testcontainers/ollama) module of Testcontainers. This dependency provide the necessary classes to spin up an **ephemeral** Docker instance for the `Ollama` service.

We define our **ChatModel** that provides the `chat` method which is the main API to interact with the chat model. 

```java
ChatModel model = OllamaChatModel.builder()
    .baseUrl(ollamaBaseUrl(ollama))
    .modelName(MODEL_NAME)
    .temperature(0.0)
    .logRequests(true)
    .logResponses(true)
    .build();
```

**NOTE:** We specify the *latest stable* version of the Ollama image when creating the **OllamaContainer**.

The quality of our testing ultimately depends on the quality of the evaluation model we use. We choose the current industry standard, the **[bespoke-minicheck model](https://ollama.com/library/bespoke-minicheck)**, which is an `open-source model` specifically trained for evaluation testing by [Bespoke Labs](https://www.bespokelabs.ai/). It ranks highly *( top at the time of writing )* of the [LLM-AggreFact](https://llm-aggrefact.github.io/) leaderboard and only produces a **yes/no** response.
  
**Below is a sample test**

```java
UserMessage userMessage = UserMessage.from("What is the name of the process by which the body breaks down food?");
ChatResponse response = model.chat(userMessage);

AiMessage aiMessage = response.aiMessage();
assertThat(aiMessage.text()).contains("digestion");
assertThat(aiMessage.toolExecutionRequests()).isEmpty();

ChatResponseMetadata metadata = response.metadata();
assertThat(metadata.modelName()).isEqualTo(MODEL_NAME);
```

## Naming Conventions

Prefer to create an interface rather than directly creating a concrete type. This allows modules and implementers to swap out these classes with minimal effort.

All interfaces should be the name of a class without additional text. For example, favour GreatClass as an interface name over 
IGreatClass or GreatClassInterface.

All implementation classes should be the name of the class with `Impl` to distinguish them from the interface. For example, GreatClassImpl.

All abstract classes should start with Base and not end with Impl. For example, BaseGreatClass.

## Deployment

To deploy this module, you need to follow the steps below; 

1. Compile the module first, type:

```bash
mvn clean install
```

2. Run your OpenMRS server, type;

```bash
mvn openmrs-sdk:run -DserverId=<your_server_id>
```

3. Deploy the module.

Deploys an artifact (OMOD) to an SDK server instance. Run this from the module's root directory, it will prompt to deploy the project.

```bash
openmrs-sdk:deploy -DserverId=<your_server_id>
```
