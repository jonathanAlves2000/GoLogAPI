-- Migração Step 7: Vincula o usuário Admin Master (admin@admin.com) à empresa master GoLogTransportes

UPDATE user_table
SET company_id = '7f564f96-d90f-42cc-beb2-e37cf63a324d'
WHERE email = 'admin@admin.com';
