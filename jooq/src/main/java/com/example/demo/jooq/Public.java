/*
 * This file is generated by jOOQ.
 */
package com.example.demo.jooq;


import com.example.demo.jooq.tables.RevokedTokens;
import com.example.demo.jooq.tables.Roles;
import com.example.demo.jooq.tables.UserRoles;
import com.example.demo.jooq.tables.Users;

import java.util.Arrays;
import java.util.List;

import org.jooq.Catalog;
import org.jooq.Table;
import org.jooq.impl.SchemaImpl;


/**
 * This class is generated by jOOQ.
 */
@SuppressWarnings({ "all", "unchecked", "rawtypes", "this-escape" })
public class Public extends SchemaImpl {

    private static final long serialVersionUID = 1L;

    /**
     * The reference instance of <code>public</code>
     */
    public static final Public PUBLIC = new Public();

    /**
     * The table <code>public.revoked_tokens</code>.
     */
    public final RevokedTokens REVOKED_TOKENS = RevokedTokens.REVOKED_TOKENS;

    /**
     * The table <code>public.roles</code>.
     */
    public final Roles ROLES = Roles.ROLES;

    /**
     * The table <code>public.user_roles</code>.
     */
    public final UserRoles USER_ROLES = UserRoles.USER_ROLES;

    /**
     * The table <code>public.users</code>.
     */
    public final Users USERS = Users.USERS;

    /**
     * No further instances allowed
     */
    private Public() {
        super("public", null);
    }


    @Override
    public Catalog getCatalog() {
        return DefaultCatalog.DEFAULT_CATALOG;
    }

    @Override
    public final List<Table<?>> getTables() {
        return Arrays.asList(
            RevokedTokens.REVOKED_TOKENS,
            Roles.ROLES,
            UserRoles.USER_ROLES,
            Users.USERS
        );
    }
}