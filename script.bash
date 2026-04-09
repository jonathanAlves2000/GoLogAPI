for i in {1..100}; do
  (
    # Captura a saída do curl na variável $resposta
    resposta=$(curl -s -X POST http://localhost:8080/login \
                 -H "Content-Type: application/json" \
                 -d '{"email": "admin@admin.com", "password": "Admin@123"}')
    
    # Imprime de forma segura e atrelada ao ID
    echo "ID $i respondeu: $resposta"
  ) &
done
wait
echo "Todas as requisições finalizaram!"
