WITH RECURSIVE ascendent_hierarchy AS
                   (SELECT rc.id,
                           rc.parent_id,
                           rc.child_id,
                           rc.relationship_configuration_parent_id,
                           rc.configuration_key,
                           rc.configuration_value
                    FROM company.relationship_configuration rc
                    WHERE rc.child_id = :companyId
                       OR (rc.parent_id = :companyId
                        AND rc.child_id IS NULL)
                    UNION ALL SELECT rc.id,
                                     rc.parent_id,
                                     rc.child_id,
                                     rc.relationship_configuration_parent_id,
                                     rc.configuration_key,
                                     rc.configuration_value
                    FROM company.relationship_configuration rc
                             INNER JOIN ascendent_hierarchy ah ON rc.id = ah.relationship_configuration_parent_id)
SELECT ah.id,
       ah.parent_id,
       ah.child_id,
       ah.relationship_configuration_parent_id,
       ah.configuration_key,
       ah.configuration_value,
       parentCompany.name AS parentName,
       childCompany.name AS childName
FROM ascendent_hierarchy ah
         LEFT JOIN company.company parentCompany ON ah.parent_id = parentCompany.id
         LEFT JOIN company.company childCompany ON ah.child_id = childCompany.id
ORDER BY (ah.parent_id = :companyId AND ah.child_id IS NULL) DESC;