package de.shyim.shopware6.xml

import com.intellij.codeInsight.completion.XmlTagInsertHandler
import com.intellij.codeInsight.lookup.LookupElement
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.psi.html.HtmlTag
import com.intellij.psi.impl.source.xml.XmlElementDescriptorProvider
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.xml.XmlTag
import com.intellij.util.indexing.FileBasedIndex
import com.intellij.xml.XmlElementDescriptor
import com.intellij.xml.XmlTagNameProvider
import de.shyim.shopware6.index.AdminComponentIndex
import de.shyim.shopware6.index.dict.AdminComponent
import gnu.trove.THashMap
import icons.ShopwareToolBoxIcons

class AdminComponentProvider : XmlTagNameProvider, XmlElementDescriptorProvider {
    override fun addTagNameVariants(elements: MutableList<LookupElement>, tag: XmlTag, prefix: String?) {
        if (tag !is HtmlTag || tag.containingFile?.originalFile?.containingDirectory?.findFile("index.js") == null) {
            return
        }

        for (key in FileBasedIndex.getInstance().getAllKeys(AdminComponentIndex.key, tag.project)) {
            val vals = FileBasedIndex.getInstance()
                .getValues(AdminComponentIndex.key, key, GlobalSearchScope.allScope(tag.project))

            vals.forEach {
                elements.add(
                    LookupElementBuilder.create(it.name)
                        .withIcon(ShopwareToolBoxIcons.SHOPWARE)
                        .withInsertHandler(XmlTagInsertHandler.INSTANCE)
                )
            }
        }
    }

    override fun getDescriptor(tag: XmlTag): XmlElementDescriptor? {
        val adminComponents = THashMap<String, AdminComponent>()

        for (key in FileBasedIndex.getInstance().getAllKeys(AdminComponentIndex.key, tag.project)) {
            val vals = FileBasedIndex.getInstance()
                .getValues(AdminComponentIndex.key, key, GlobalSearchScope.allScope(tag.project))

            vals.forEach {
                adminComponents[it.name] = it
            }
        }

        val props = HashSet<String>()

        if (!adminComponents.containsKey(tag.name)) {
            return null
        }

        val component = adminComponents.get(tag.name)
        props.addAll(component!!.props)

        if (component.extends != null) {
            addParentProps(props, adminComponents, component.extends!!)
        }

        return AdminComponentTagDescriptor(
            component,
            tag,
            props
        )
    }

    private fun addParentProps(
        props: HashSet<String>,
        adminComponents: THashMap<String, AdminComponent>,
        extends: String
    ) {
        if (!adminComponents.containsKey(extends)) {
            return
        }

        val innerComp = adminComponents.get(extends)

        innerComp!!.props.forEach {
            if (!props.contains(it)) {
                props.add(it)
            }
        }

        if (innerComp.extends != null) {
            addParentProps(props, adminComponents, innerComp.extends!!)
        }
    }
}