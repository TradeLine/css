# css
Использование:
apply plugin: "jsResourceBuilder"

buildResource {
    def imageFolder = "$projectDir/images"
    outFile = file("$out/resources.js")
    css {
        images {
            add("checked_png", file("$imageFolder/checked.png"))
            add("checked_hover_png", file("$imageFolder/checked_hover.png"))
        }
    }
}