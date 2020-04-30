/*
 * Copyright (C) 2019 - 2020 Rabobank Nederland
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.rabobank.argos.service.domain.security;

import com.rabobank.argos.domain.hierarchy.HierarchyMode;
import com.rabobank.argos.domain.hierarchy.TreeNode;
import com.rabobank.argos.domain.permission.Permission;
import com.rabobank.argos.service.domain.hierarchy.HierarchyRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static java.util.Collections.emptySet;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DefaultLocalPermissionCheckStrategyTest {

    private static final String ACCOUNT_NAME = "accountName";
    private static final String LABEL_ID = "labelId";
    private static final String PARENT_LABEL_ID = "parentLabelId";
    @Mock
    private HierarchyRepository hierarchyRepository;

    @Mock
    private LocalPermissionCheckData localPermissionCheckData;

    @Mock
    private AccountSecurityContext accountSecurityContext;

    private DefaultLocalPermissionCheckStrategy strategy;

    @Mock
    private TreeNode treeNode;

    @BeforeEach
    void setUp() {
        strategy = new DefaultLocalPermissionCheckStrategy(hierarchyRepository, accountSecurityContext);
    }

    @Test
    void hasLocalPermissionOnLabel() {
        when(localPermissionCheckData.getLabelIds()).thenReturn(new HashSet<>(List.of(LABEL_ID)));
        when(hierarchyRepository.getSubTree(LABEL_ID, HierarchyMode.NONE, 0)).thenReturn(Optional.of(treeNode));
        when(treeNode.getIdPathToRoot()).thenReturn(Collections.emptyList());
        when(accountSecurityContext.allLocalPermissions(Collections.singletonList(LABEL_ID))).thenReturn(Set.of(Permission.READ));
        assertThat(strategy.hasLocalPermission(localPermissionCheckData, new HashSet<>(List.of(Permission.READ))), is(true));
    }

    @Test
    void hasImplicitReadLocalPermissionOnLabel() {
        when(localPermissionCheckData.getLabelIds()).thenReturn(new HashSet<>(List.of(LABEL_ID)));
        when(hierarchyRepository.getSubTree(LABEL_ID, HierarchyMode.NONE, 0)).thenReturn(Optional.of(treeNode));
        when(treeNode.getIdPathToRoot()).thenReturn(Collections.emptyList());
        when(accountSecurityContext.allLocalPermissions(Collections.singletonList(LABEL_ID))).thenReturn(Set.of(Permission.TREE_EDIT));
        assertThat(strategy.hasLocalPermission(localPermissionCheckData, new HashSet<>(List.of(Permission.READ))), is(true));
    }

    @Test
    void hasMultipleLocalPermissionOnLabel() {
        when(localPermissionCheckData.getLabelIds()).thenReturn(new HashSet<>(List.of(LABEL_ID)));
        when(hierarchyRepository.getSubTree(LABEL_ID, HierarchyMode.NONE, 0)).thenReturn(Optional.of(treeNode));
        when(treeNode.getIdPathToRoot()).thenReturn(Collections.emptyList());
        when(accountSecurityContext.allLocalPermissions(Collections.singletonList(LABEL_ID))).thenReturn(Set.of(Permission.READ));
        assertThat(strategy.hasLocalPermission(localPermissionCheckData, new HashSet<>(List.of(Permission.VERIFY, Permission.READ))), is(true));
    }

    @Test
    void hasLocalPermissionOnParentLabel() {
        when(localPermissionCheckData.getLabelIds()).thenReturn(new HashSet<>(List.of(LABEL_ID)));
        when(hierarchyRepository.getSubTree(LABEL_ID, HierarchyMode.NONE, 0)).thenReturn(Optional.of(treeNode));
        when(treeNode.getIdPathToRoot()).thenReturn(List.of(PARENT_LABEL_ID));
        when(accountSecurityContext.allLocalPermissions(List.of(PARENT_LABEL_ID, LABEL_ID))).thenReturn(Set.of(Permission.READ));
        assertThat(strategy.hasLocalPermission(localPermissionCheckData, new HashSet<>(List.of(Permission.READ))), is(true));
    }

    @Test
    void hasNoLocalPermissionOnLabel() {
        when(localPermissionCheckData.getLabelIds()).thenReturn(new HashSet<>(List.of(LABEL_ID)));
        when(hierarchyRepository.getSubTree(LABEL_ID, HierarchyMode.NONE, 0)).thenReturn(Optional.of(treeNode));
        when(treeNode.getIdPathToRoot()).thenReturn(Collections.emptyList());
        when(accountSecurityContext.allLocalPermissions(Collections.singletonList(LABEL_ID))).thenReturn(Set.of(Permission.READ));
        assertThat(strategy.hasLocalPermission(localPermissionCheckData, new HashSet<>(List.of(Permission.VERIFY))), is(false));
    }

    @Test
    void hasNoLocalPermissionNoLabelId() {
        when(localPermissionCheckData.getLabelIds()).thenReturn(new HashSet<>());
        assertThat(strategy.hasLocalPermission(localPermissionCheckData, new HashSet<>(List.of(Permission.VERIFY))), is(false));
    }

    @Test
    void hasLocalPermissionOnLocalPermissions() {
        when(localPermissionCheckData.getLabelIds()).thenReturn(new HashSet<>(List.of(LABEL_ID)));
        when(hierarchyRepository.getSubTree(LABEL_ID, HierarchyMode.NONE, 0)).thenReturn(Optional.of(treeNode));
        when(treeNode.getIdPathToRoot()).thenReturn(Collections.emptyList());
        when(accountSecurityContext.allLocalPermissions(any())).thenReturn(emptySet());
        assertThat(strategy.hasLocalPermission(localPermissionCheckData, new HashSet<>(List.of(Permission.READ))), is(false));
    }

    @Test
    void hasNoLocalPermissionOtherLocalPermissions() {
        when(localPermissionCheckData.getLabelIds()).thenReturn(new HashSet<>(List.of(LABEL_ID)));
        when(hierarchyRepository.getSubTree(LABEL_ID, HierarchyMode.NONE, 0)).thenReturn(Optional.of(treeNode));
        when(treeNode.getIdPathToRoot()).thenReturn(Collections.emptyList());
        when(accountSecurityContext.allLocalPermissions(any())).thenReturn(emptySet());
        assertThat(strategy.hasLocalPermission(localPermissionCheckData, new HashSet<>(List.of(Permission.READ))), is(false));
    }
}