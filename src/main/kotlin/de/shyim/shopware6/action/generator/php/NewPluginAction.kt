package de.shyim.shopware6.action.generator.php

import com.intellij.ide.highlighter.XmlFileType
import com.intellij.json.JsonFileType
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.LangDataKeys
import com.intellij.openapi.fileTypes.PlainTextFileType
import com.intellij.openapi.project.DumbAwareAction
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.Messages
import com.intellij.psi.PsiDirectory
import com.jetbrains.php.lang.PhpFileType
import de.shyim.shopware6.action.generator.ActionUtil
import de.shyim.shopware6.templates.ShopwareTemplates
import icons.ShopwareToolBoxIcons

class NewPluginAction : DumbAwareAction("Create a Plugin", "Create a new Plugin", ShopwareToolBoxIcons.SHOPWARE) {
    override fun actionPerformed(e: AnActionEvent) {
        val wrapper = NewPluginDialogWrapper()
        val config = wrapper.showAndGetConfig() ?: return

        // Create folder

        val rootDirectory = ActionUtil.getViewDirectory(e.dataContext) ?: return

        if (rootDirectory.findSubdirectory(config.name) != null) {
            Messages.showInfoMessage("Plugin already exists", "Error")
            return
        }

        val pluginFolder = ActionUtil.createDirectory(rootDirectory, config.name) ?: return

        // Create composer.json
        val content = ShopwareTemplates.applyShopwarePluginComposerJson(
            e.project!!,
            config
        )

        ActionUtil.createFile(
            e.project!!,
            JsonFileType.INSTANCE,
            "composer.json",
            content,
            pluginFolder
        )

        // Changelogs

        createChangelog(e.project!!, pluginFolder, "CHANGELOG_de-DE.md", config)
        createChangelog(e.project!!, pluginFolder, "CHANGELOG_en-GB.md", config)

        // Create bootstrap

        val srcFolder = ActionUtil.createDirectory(pluginFolder, "src") ?: return

        val pluginContent = ShopwareTemplates.applyShopwarePluginBootstrap(
            e.project!!,
            config
        )

        val pluginBootstrapFile = ActionUtil.createFile(
            e.project!!,
            PhpFileType.INSTANCE,
            config.name + ".php",
            pluginContent,
            srcFolder
        )

        // Create default resources

        val resourcesFolder = ActionUtil.createDirectory(srcFolder, "Resources") ?: return
        val configFolder = ActionUtil.createDirectory(resourcesFolder, "config") ?: return

        ActionUtil.createFile(
            e.project!!,
            XmlFileType.INSTANCE,
            "config.xml",
            ShopwareTemplates.applyShopwarePluginConfig(e.project!!),
            configFolder
        )

        ActionUtil.createFile(
            e.project!!,
            XmlFileType.INSTANCE,
            "services.xml",
            ShopwareTemplates.applyShopwarePluginServicesXml(e.project!!, config),
            configFolder
        )

        val view = LangDataKeys.IDE_VIEW.getData(e.dataContext) ?: return
        view.selectElement(pluginBootstrapFile)
    }

    private fun createChangelog(project: Project, directory: PsiDirectory, name: String, config: NewPluginConfig) {
        val content = ShopwareTemplates.applyShopwarePluginChangelog(
            project,
            config
        )

        ActionUtil.createFile(
            project,
            PlainTextFileType.INSTANCE,
            name,
            content,
            directory
        )
    }
}