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
package com.rabobank.argos.service.domain.hierarchy;

import com.rabobank.argos.domain.hierarchy.TreeNode;
import com.rabobank.argos.domain.hierarchy.TreeNodeVisitor;
import com.rabobank.argos.domain.permission.Permission;
import com.rabobank.argos.service.domain.security.AccountSecurityContext;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static com.rabobank.argos.domain.permission.Permission.*;

public class AccountPermissionTreeNodeVisitor implements TreeNodeVisitor<Optional<TreeNode>> {

    private TreeNode treeNodeWithUserPermissions;
    private HashMap<String, TreeNode> parentRegistry = new HashMap<>();
    private final AccountSecurityContext accountSecurityContext;
    private Set<Permission> hierarchyPermissions = EnumSet.of(READ,TREE_EDIT,LOCAL_PERMISSION_EDIT,LINK_ADD,LAYOUT_ADD,VERIFY,NPA_EDIT);

    AccountPermissionTreeNodeVisitor(final AccountSecurityContext accountSecurityContext) {
        this.accountSecurityContext = accountSecurityContext;
    }

    @Override
    public boolean visitEnter(TreeNode treeNode) {
        TreeNode copyOfTreeNode = treeNode
                .withChildren(new ArrayList<>())
                .withPermissions(determineAggregatedPermissions(treeNode));

        if (copyOfTreeNode.getPermissions().isEmpty() && nodeHasNoPermissionsUpTree(treeNode.getIdsOfDescendantLabels())) {
            return false;
        }

        if (treeNodeWithUserPermissions == null) {
            treeNodeWithUserPermissions = copyOfTreeNode;

        } else {

            TreeNode parent = parentRegistry.get(copyOfTreeNode.getParentLabelId());
            parent.addChild(copyOfTreeNode);
        }

        parentRegistry.put(copyOfTreeNode.getReferenceId(), copyOfTreeNode);
        return true;
    }

    private boolean nodeHasNoPermissionsUpTree(List<String> idsOfDescendantLabels) {

        return accountSecurityContext
                .allLocalPermissions(idsOfDescendantLabels)
                .isEmpty();
    }

    private List<Permission> determineAggregatedPermissions(TreeNode treeNode) {
        Set<Permission> aggregatedPermissions = new HashSet<>();
        List<String> labelIdsDownTree = new ArrayList<>(treeNode.getIdPathToRoot());
        if (!treeNode.isLeafNode()) {
            labelIdsDownTree.add(treeNode.getReferenceId());
        }
        aggregatedPermissions.addAll(accountSecurityContext.allLocalPermissions(labelIdsDownTree));
        aggregatedPermissions.addAll(accountSecurityContext.getGlobalPermission());
        Set<Permission> hierarchyOnlyPermissions = filterForHierarchyOnlyPermissions(aggregatedPermissions);
        List<Permission> arrayOfHierarchyOnlyPermissions = new ArrayList<>(hierarchyOnlyPermissions);
        arrayOfHierarchyOnlyPermissions.sort(Comparator.comparing(Permission::name));
        return arrayOfHierarchyOnlyPermissions;
    }

    private Set<Permission> filterForHierarchyOnlyPermissions(Set<Permission> aggregatedPermissions) {
       return  aggregatedPermissions
                .stream()
                .filter(permission-> hierarchyPermissions.contains(permission))
                .collect(Collectors.toSet());
    }

    @Override
    public boolean visitExit(TreeNode treeNode) {
        return true;
    }

    @Override
    public boolean visitLeaf(TreeNode treeNode) {

        TreeNode copyOfTreeNode = treeNode.withPermissions(determineAggregatedPermissions(treeNode));

        if (copyOfTreeNode.getPermissions().isEmpty()) {
            return false;
        }

        if (treeNodeWithUserPermissions == null) {
            treeNodeWithUserPermissions = copyOfTreeNode;
        }

        if (parentRegistry.containsKey(copyOfTreeNode.getParentLabelId())) {
            TreeNode parent = parentRegistry.get(copyOfTreeNode.getParentLabelId());
            parent.addChild(copyOfTreeNode);
        }

        return true;
    }

    @Override
    public Optional<TreeNode> result() {
        return Optional.ofNullable(treeNodeWithUserPermissions);
    }
}
