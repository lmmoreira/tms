WITH RECURSIVE descendent_hierarchy AS
                   (SELECT rc.id,
                           rc.parent_id,
                           rc.child_id,
                           rc.relationship_configuration_parent_id,
                           rc.configuration_key,
                           rc.configuration_value
                    FROM company.relationship_configuration rc
                    WHERE rc.parent_id = :companyId
                      AND rc.child_id IS NULL
                    UNION ALL SELECT rc.id,
                                     rc.parent_id,
                                     rc.child_id,
                                     rc.relationship_configuration_parent_id,
                                     rc.configuration_key,
                                     rc.configuration_value
                    FROM company.relationship_configuration rc
                             INNER JOIN descendent_hierarchy dh ON rc.relationship_configuration_parent_id = dh.id)
SELECT dh.id,
       dh.parent_id,
       dh.child_id,
       dh.relationship_configuration_parent_id,
       dh.configuration_key,
       dh.configuration_value,
       parentCompany.name AS parent_name,
       childCompany.name AS child_name
FROM descendent_hierarchy dh
         LEFT JOIN company.company parentCompany ON dh.parent_id = parentCompany.id
         LEFT JOIN company.company childCompany ON dh.child_id = childCompany.id
ORDER BY (dh.parent_id = :companyId AND dh.child_id IS NULL) DESC;