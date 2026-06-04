package GoLogAPI.service.routeOptimization;

import com.google.maps.model.EncodedPolyline;
import com.google.maps.model.LatLng;
import org.springframework.stereotype.Service;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Deque;
import java.util.List;
import java.util.stream.IntStream;

@Service
public class OptimizeListLocation {

    //Orquestra a decodificação e a simplificação da rota vinda da API do Google.
    public static List<LatLng> procesingRouteGoogle(String pointsApi) {
        if (pointsApi == null || pointsApi.isEmpty()) {
            return new ArrayList<>();
        }

        EncodedPolyline encodedPolyline = new EncodedPolyline(pointsApi);
        List<LatLng> points = encodedPolyline.decodePath();

        double tolerancyMetros = 10.0;

        //Retorna a rota já simplificada pelo algoritmo RDP
        return simplifyPolyline(points, tolerancyMetros);
    }


    //Método responsável por simplificar uma Polyline. Utiliza o algoritmo de Ramer-Douglas-Peucker adaptado.
    public static List<LatLng> simplifyPolyline(List<LatLng> poly, Double tolerancia) {
        if (poly.isEmpty())
            throw new IllegalArgumentException("A polyline deve ter pelo menos um ponto");
        if (tolerancia < 0)
            throw new IllegalArgumentException("A tolerância deve ser maior que zero");

        int n = poly.size();
        if (n < 2)
            return poly;

        boolean[] manterPonto = new boolean[n];
        manterPonto[0] = true;
        manterPonto[n - 1] = true;

        Deque<int[]> stack = new ArrayDeque<>();
        stack.addLast(new int[] { 0, n - 1 });

        while (!stack.isEmpty()) {
            int[] segment = stack.removeLast();
            int inicio = segment[0];
            int fim = segment[1];

            double maxDist = 0.0;
            int maxIdx = 0;

            for (int idx = inicio + 1; idx < fim; idx++) {
                double dist = distanciaDaLinha(poly.get(idx), poly.get(inicio), poly.get(fim));
                if (dist > maxDist) {
                    maxDist = dist;
                    maxIdx = idx;
                }
            }
            if (maxDist > tolerancia) {
                manterPonto[maxIdx] = true;
                stack.addLast(new int[]{inicio, maxIdx});
                stack.addLast(new int[]{maxIdx, fim});
            }
        }

        List<LatLng> result = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            if (manterPonto[i]) {
                result.add(poly.get(i));
            }
        }
        return result;
    }


    //Método responsável por retornar o índice da coordenada mais próxima de uma coordenada alvo.
    public static int indiceMaisProxima(List<LatLng> lista, LatLng alvo) {
        if (lista == null || lista.isEmpty())
            return -1;

        //Ajustado para chamar o método estático da própria classe interna
        return IntStream.range(0, lista.size()).boxed()
                .min(Comparator.comparingDouble(i -> calculaDistancia(alvo, lista.get(i))))
                .orElse(-1);
    }


    //Método responsável por calcular a distância mínima de um ponto a um segmento de reta.
    public static double distanciaDaLinha(LatLng ponto, LatLng inicio, LatLng fim) {
        if (inicio.lat == fim.lat && inicio.lng == fim.lng) return calculaDistancia(fim, ponto);

        double s0lat = Math.toRadians(ponto.lat);
        double s0lng = Math.toRadians(ponto.lng);
        double s1lat = Math.toRadians(inicio.lat);
        double s1lng = Math.toRadians(inicio.lng);
        double s2lat = Math.toRadians(fim.lat);
        double s2lng = Math.toRadians(fim.lng);

        double lonCorrection = Math.cos(s1lat);
        double s2s1lat = s2lat - s1lat;
        double s2s1lng = (s2lng - s1lng) * lonCorrection;

        double u = ((s0lat - s1lat) * s2s1lat + (s0lng - s1lng) * lonCorrection * s2s1lng)
                / (s2s1lat * s2s1lat + s2s1lng * s2s1lng);

        if (u <= 0)
            return calculaDistancia(ponto, inicio);
        if (u >= 1)
            return calculaDistancia(ponto, fim);

        LatLng su = new LatLng(
                inicio.lat + u * (fim.lat - inicio.lat),
                inicio.lng + u * (fim.lng - inicio.lng)
        );

        return calculaDistancia(ponto, su);
    }


     //Método responsável por calcular a distância entre dois pontos (Fórmula de Haversine).
    public static double calculaDistancia(LatLng inicio, LatLng fim) {
        final double R = 6371000; // Raio da Terra em metros
        double dLat = Math.toRadians(fim.lat - inicio.lat);
        double dLng = Math.toRadians(fim.lng - inicio.lng);

        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(inicio.lat)) * Math.cos(Math.toRadians(fim.lat)) *
                        Math.sin(dLng / 2) * Math.sin(dLng / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return R * c;
    }
}
