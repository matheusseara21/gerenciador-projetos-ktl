
# Gerenciador de Projetos

Um aplicativo Android para gerenciamento de projetos e tarefas, desenvolvido com Jetpack Compose e Kotlin.

![Android](https://img.shields.io/badge/Android-3DDC84?style=for-the-badge&logo=android&logoColor=white)
![Kotlin](https://img.shields.io/badge/Kotlin-0095D5?style=for-the-badge&logo=kotlin&logoColor=white)
![Jetpack Compose](https://img.shields.io/badge/Jetpack%20Compose-4285F4?style=for-the-badge&logo=jetpackcompose&logoColor=white)

## Sobre o Projeto

O Gerenciador de Projetos é uma solução para organizar projetos, tarefas e acompanhar o tempo de trabalho. Oferece recursos como importação de issues do GitHub e timer integrado.

### Principais Funcionalidades

- Gerenciamento completo de projetos (criar, editar, deletar)
- Sistema de tarefas com status e descrições
- Timer integrado para rastrear tempo de trabalho
- Importação de issues do GitHub como tarefas
- Compartilhamento de relatórios
- Persistência local com Room Database
- Interface moderna com Material Design 3

## Tecnologias Utilizadas

### Core
- Kotlin
- Jetpack Compose
- Material Design 3

### Arquitetura
- MVVM (Model-View-ViewModel)
- Clean Architecture
- Repository Pattern

### Bibliotecas
- Room - Banco de dados local
- Hilt - Injeção de dependências
- Retrofit - Comunicação com API REST
- Coroutines - Programação assíncrona
- Flow - Streams reativos
- Navigation Compose - Navegação entre telas

### API Externa
- GitHub API - Importação de issues

## Estrutura do Projeto

```
app/
├── data/
│   ├── adapter/        # Conversores (GitHub -> Task)
│   ├── dao/            # Data Access Objects
│   ├── database/       # Configuração Room
│   ├── model/          # Entidades (Project, Task)
│   ├── remote/         # API Client & Models
│   └── repository/     # Repositórios
├── di/                 # Módulos Hilt (DI)
├── presentation/
│   ├── navigation/     # Rotas e navegação
│   ├── screens/        # Telas Compose
│   └── viewmodel/      # ViewModels
├── ui/
│   └── theme/          # Cores e tema
└── util/               # Utilitários (ShareUtil, TimeUtil)
```

## Instalação

### Pré-requisitos

- Android Studio Hedgehog ou superior
- JDK 17 ou superior
- Gradle 8.0+
- Android SDK 24+ (mínimo) / 35 (compilação)

### Passos

1. Clone o repositório
```bash
git clone https://github.com/seu-usuario/gerenciador.git
cd gerenciador
```

2. Abra no Android Studio
```
File -> Open -> Selecione a pasta do projeto
```

3. Sincronize o Gradle
```
File -> Sync Project with Gradle Files
```

4. Execute o projeto
```
Run -> Run 'app'
```

## Funcionalidades Detalhadas

### Gerenciamento de Projetos

- Criar projetos com nome, descrição, cliente e prazo
- Status: Em Andamento, Concluído, Cancelado
- Editar e deletar projetos
- Exclusão em cascata de tarefas

### Sistema de Tarefas

- CRUD completo de tarefas
- Status: Pendente, Em Andamento, Concluída, Cancelada
- Vinculação a projetos
- Descrições detalhadas

### Timer de Trabalho

- Play/Pause por tarefa
- Acumulação de tempo
- Formato HH:MM:SS
- Persistência no banco
- Auto-pause ao sair da tela

### Importação GitHub

1. Toque em "Importar do GitHub"
2. Digite owner (ex: facebook)
3. Digite repo (ex: react)
4. Issues são importadas como tarefas

Processo:
- Cria projeto automaticamente
- Converte issues em tarefas
- Status open = Pendente
- Status closed = Concluída

### Compartilhamento

- Gera relatórios de projetos
- Nome, cliente, prazo e progresso
- Lista completa de tarefas
- Compartilha via WhatsApp, Email, etc.

## Banco de Dados

### Tabela: projects

```sql
CREATE TABLE projects (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    nome TEXT NOT NULL,
    descricao TEXT NOT NULL,
    cliente TEXT NOT NULL,
    deadline INTEGER NOT NULL,
    dataCriacao INTEGER NOT NULL,
    status TEXT NOT NULL
)
```

### Tabela: tasks

```sql
CREATE TABLE tasks (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    projectId INTEGER NOT NULL,
    titulo TEXT NOT NULL,
    descricao TEXT NOT NULL,
    dataCriacao INTEGER NOT NULL,
    dataConclusao INTEGER,
    status TEXT NOT NULL,
    tempoTrabalhado INTEGER NOT NULL DEFAULT 0,
    timerAtivo INTEGER NOT NULL DEFAULT 0,
    ultimoInicioTimer INTEGER NOT NULL DEFAULT 0,
    FOREIGN KEY(projectId) REFERENCES projects(id) ON DELETE CASCADE
)
```

### Migration v1 para v2

Adiciona campos de timer na tabela tasks mantendo dados existentes.

## API GitHub

### Endpoint

```
GET https://api.github.com/repos/{owner}/{repo}/issues
```

### Autenticação

- Não requer token para repositórios públicos
- Limite: 60 requisições/hora sem autenticação

## Build

### Gerar APK Debug

```bash
./gradlew assembleDebug
```

Saída: `app/build/outputs/apk/debug/app-debug.apk`

### Gerar APK Release

```bash
./gradlew assembleRelease
```

## Configuração

### build.gradle.kts (App)

```kotlin
dependencies {
    // Room
    implementation("androidx.room:room-runtime:2.6.1")
    implementation("androidx.room:room-ktx:2.6.1")
    kapt("androidx.room:room-compiler:2.6.1")
    
    // Hilt
    implementation(libs.hilt.android)
    kapt(libs.hilt.compiler)
    implementation(libs.hilt.navigation.compose)
    
    // Retrofit
    implementation(libs.squareup.retrofit)
    implementation(libs.squareup.converter.gson)
    
    // Coroutines
    implementation(libs.kotlinx.coroutines.android)
}
```

## Autores

- Matheus & Igor - Desenvolvedores
