import { BrowserRouter, Navigate, Route, Routes } from 'react-router-dom';
import { useAuth } from './auth/AuthContext';
import { RutaProtegida } from './auth/RutaProtegida';
import { LoginPage } from './auth/LoginPage';
import { AtencionLlamadaPage } from './llamada/AtencionLlamadaPage';
import { TableroPage } from './tablero/TableroPage';
import { TicketDetallePage } from './ticket/TicketDetallePage';
import { MetricasPage } from './metricas/MetricasPage';
import { Layout } from './shared/Layout';

function RaizRedireccion() {
  const { usuario, cargando } = useAuth();
  if (cargando) return <div className="pagina-cargando">Cargando…</div>;
  return <Navigate to={usuario ? '/tablero' : '/login'} replace />;
}

export function AppRouter() {
  return (
    <BrowserRouter>
      <Routes>
        <Route path="/login" element={<LoginPage />} />
        <Route path="/" element={<RaizRedireccion />} />
        <Route
          path="/atencion-llamada"
          element={
            <RutaProtegida rolesPermitidos={['AGENTE', 'SUPERVISOR']}>
              <Layout>
                <AtencionLlamadaPage />
              </Layout>
            </RutaProtegida>
          }
        />
        <Route
          path="/tablero"
          element={
            <RutaProtegida>
              <Layout>
                <TableroPage />
              </Layout>
            </RutaProtegida>
          }
        />
        <Route
          path="/tickets/:codigo"
          element={
            <RutaProtegida>
              <Layout>
                <TicketDetallePage />
              </Layout>
            </RutaProtegida>
          }
        />
        <Route
          path="/metricas"
          element={
            <RutaProtegida rolesPermitidos={['SUPERVISOR', 'ADMIN']}>
              <Layout>
                <MetricasPage />
              </Layout>
            </RutaProtegida>
          }
        />
        <Route path="*" element={<Navigate to="/" replace />} />
      </Routes>
    </BrowserRouter>
  );
}
