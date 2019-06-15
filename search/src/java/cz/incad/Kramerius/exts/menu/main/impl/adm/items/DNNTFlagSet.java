package cz.incad.Kramerius.exts.menu.main.impl.adm.items;

import cz.incad.Kramerius.exts.menu.main.impl.AbstractMainMenuItem;
import cz.incad.Kramerius.exts.menu.main.impl.adm.AdminMenuItem;
import cz.incad.kramerius.security.SecuredActions;

import java.io.IOException;

public class DNNTFlagSet extends AbstractMainMenuItem implements AdminMenuItem {

    @Override
    public boolean isRenderable() {

        return (hasUserAllowedAction(SecuredActions.ADMINISTRATE.getFormalName()) || hasUserAllowedAction(SecuredActions.DNNT_ADMIN.getFormalName()));
    }

    @Override
    public String getRenderedItem() throws IOException {
        return renderMainMenuItem(
                "javascript:dnntFlagSet(); javascript:hideAdminMenu();",
                "administrator.menu.dialogs.dnnt.title", false);
    }

}
