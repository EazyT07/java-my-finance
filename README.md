# 📊 Meine Finanzen (MyFinance)

> A lightweight, privacy-focused JavaFX desktop application for personal finance tracking, CSV bank statement imports, and dynamic multi-dimensional pivot analysis.

---

## 🌟 Key Features

* **📥 Robust CSV Import:** Seamlessly import transaction data with flexible date parsing (`dd.MM.yyyy` and `dd.MM.yy`) and automated field mapping.
* **🔒 100% Local & Private:** Powered by an embedded **SQLite** database (`database.db`). Your financial data stays entirely on your machine.
* **📐 Dynamic Pivot Analysis Engine:**
  * **Flexible Row Grouping:** Slices expenses by **Category**, **Subcategory**, or combined **Category & Subcategory**.
  * **Time Horizons:** View data aggregated across **Years** or **Months**.
  * **Custom Year Filter:** Easily restrict analysis ranges (e.g., last 5 years) or view all historical data.
  * **Automated Totals:** Dynamically computed row totals and column grand total summary rows with visual bold styling.
* **💡 Net Spending Accounting Logic:**
  * **Expenses ($+$):** Displayed as negative values representing total category spending.
  * **Income ($-$)**: Displayed as positive values 
  * **Net Balance:** Summary row instantly shows if your period ended in a surplus.
* **🖥️ Native macOS & Desktop Polish:** High-DPI icons, customized table styling with German currency formatting (`1.234,56 €`), and single-click native executable packaging (`.dmg`).

---

## 🏗️ Architecture & Tech Stack

| Technology | Role |
| :--- | :--- |
| **Java 21** | Core Runtime |
| **JavaFX 21** | UI Framework & Controls |
| **SQLite JDBC** | Embedded Local Database |
| **Apache Maven** | Build System & Dependency Management |
| **jpackage** | Native Packaging Tool |

### Architecture Highlights
* **Decoupled Engine:** The `AnalysisEngine` handles pure matrix/pivot math ($O(N)$ single-pass aggregation) independent of the JavaFX UI thread.
* **Data Transfer Objects:** Uses immutable Java Records (`AggregationResult`) to transport aggregated pivot data cleanly to `AnalysisView`.

---

## 🚀 Getting Started

### Prerequisites
* **JDK 21** or higher
* **Apache Maven 3.8+**

### Building & Running from Source

1. **Navigate to the cloned repository directory:**
    cd <your-repository-folder>

2. **Compile the project dependencies and code:**
    mvn clean compile

3. **Launch the desktop application:**
    mvn javafx:run

## 📦 Packaging as a Native macOS Installer
1. **Build the executable JAR file:**
    mvn clean package

2. **Generate the native .dmg installer using jpackage:**
    jpackage \
        --type dmg \
        --name "MyFinance" \
        --app-version "1.0.0" \
        --vendor "FinanceApp" \
        --icon icon.icns \
        --input target/ \
        --main-jar MyFinance-1.0-SNAPSHOT.jar \
        --main-class com.financeapp.Launcher \
        --dest dist/


