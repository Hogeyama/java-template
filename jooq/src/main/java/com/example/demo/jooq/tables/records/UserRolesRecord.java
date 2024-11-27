/*
 * This file is generated by jOOQ.
 */
package com.example.demo.jooq.tables.records;


import com.example.demo.jooq.tables.UserRoles;

import java.time.OffsetDateTime;

import org.jooq.Record2;
import org.jooq.impl.UpdatableRecordImpl;


/**
 * This class is generated by jOOQ.
 */
@SuppressWarnings({ "all", "unchecked", "rawtypes", "this-escape" })
public class UserRolesRecord extends UpdatableRecordImpl<UserRolesRecord> {

    private static final long serialVersionUID = 1L;

    /**
     * Setter for <code>public.user_roles.user_id</code>.
     */
    public UserRolesRecord setUserId(Long value) {
        set(0, value);
        return this;
    }

    /**
     * Getter for <code>public.user_roles.user_id</code>.
     */
    public Long getUserId() {
        return (Long) get(0);
    }

    /**
     * Setter for <code>public.user_roles.role_id</code>.
     */
    public UserRolesRecord setRoleId(Long value) {
        set(1, value);
        return this;
    }

    /**
     * Getter for <code>public.user_roles.role_id</code>.
     */
    public Long getRoleId() {
        return (Long) get(1);
    }

    /**
     * Setter for <code>public.user_roles.created_at</code>.
     */
    public UserRolesRecord setCreatedAt(OffsetDateTime value) {
        set(2, value);
        return this;
    }

    /**
     * Getter for <code>public.user_roles.created_at</code>.
     */
    public OffsetDateTime getCreatedAt() {
        return (OffsetDateTime) get(2);
    }

    // -------------------------------------------------------------------------
    // Primary key information
    // -------------------------------------------------------------------------

    @Override
    public Record2<Long, Long> key() {
        return (Record2) super.key();
    }

    // -------------------------------------------------------------------------
    // Constructors
    // -------------------------------------------------------------------------

    /**
     * Create a detached UserRolesRecord
     */
    public UserRolesRecord() {
        super(UserRoles.USER_ROLES);
    }

    /**
     * Create a detached, initialised UserRolesRecord
     */
    public UserRolesRecord(Long userId, Long roleId, OffsetDateTime createdAt) {
        super(UserRoles.USER_ROLES);

        setUserId(userId);
        setRoleId(roleId);
        setCreatedAt(createdAt);
        resetChangedOnNotNull();
    }

    /**
     * Create a detached, initialised UserRolesRecord
     */
    public UserRolesRecord(com.example.demo.jooq.tables.pojos.UserRoles value) {
        super(UserRoles.USER_ROLES);

        if (value != null) {
            setUserId(value.getUserId());
            setRoleId(value.getRoleId());
            setCreatedAt(value.getCreatedAt());
            resetChangedOnNotNull();
        }
    }
}