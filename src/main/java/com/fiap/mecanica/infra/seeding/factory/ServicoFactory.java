package com.fiap.mecanica.infra.seeding.factory;

import com.fiap.mecanica.domain.enums.CategoriaServico;
import com.fiap.mecanica.domain.model.Servico;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import org.springframework.stereotype.Component;

@Component
public class ServicoFactory extends SeederFactory<Servico> {

  private static final List<ServicoTemplate> TEMPLATES = new ArrayList<>();
  private final Queue<ServicoTemplate> templateQueue = new LinkedList<>();

  static {
    // --- MANUTENCAO_PREVENTIVA (20 items) ---
    add(
        "Troca de Óleo Mineral",
        "Substituição de óleo mineral e filtro de óleo",
        150.0,
        200.0,
        30,
        45,
        CategoriaServico.MANUTENCAO_PREVENTIVA);
    add(
        "Troca de Óleo Sintético",
        "Substituição de óleo sintético de alta performance e filtro",
        250.0,
        350.0,
        30,
        45,
        CategoriaServico.MANUTENCAO_PREVENTIVA);
    add(
        "Alinhamento 3D",
        "Alinhamento computadorizado das 4 rodas",
        80.0,
        120.0,
        40,
        60,
        CategoriaServico.MANUTENCAO_PREVENTIVA);
    add(
        "Balanceamento de Rodas",
        "Balanceamento estático e dinâmico das 4 rodas",
        60.0,
        100.0,
        30,
        50,
        CategoriaServico.MANUTENCAO_PREVENTIVA);
    add(
        "Rodízio de Pneus",
        "Troca de posição dos pneus para garantir desgaste uniforme",
        40.0,
        60.0,
        20,
        30,
        CategoriaServico.MANUTENCAO_PREVENTIVA);
    add(
        "Revisão de 10.000km",
        "Check-up completo conforme manual do fabricante para 10k km",
        400.0,
        600.0,
        120,
        180,
        CategoriaServico.MANUTENCAO_PREVENTIVA);
    add(
        "Troca de Filtro de Ar",
        "Substituição do filtro de ar do motor para melhor combustão",
        50.0,
        80.0,
        15,
        20,
        CategoriaServico.MANUTENCAO_PREVENTIVA);
    add(
        "Troca de Filtro de Cabine",
        "Substituição do filtro de ar condicionado (antipólen)",
        60.0,
        90.0,
        20,
        30,
        CategoriaServico.MANUTENCAO_PREVENTIVA);
    add(
        "Troca de Filtro de Combustível",
        "Substituição preventiva do filtro de combustível",
        70.0,
        100.0,
        30,
        45,
        CategoriaServico.MANUTENCAO_PREVENTIVA);
    add(
        "Troca de Fluido de Freio",
        "Sangria e substituição completa do fluido DOT4",
        180.0,
        250.0,
        60,
        90,
        CategoriaServico.MANUTENCAO_PREVENTIVA);
    add(
        "Troca de Líquido de Arrefecimento",
        "Limpeza do sistema e troca do aditivo do radiador",
        200.0,
        300.0,
        60,
        90,
        CategoriaServico.MANUTENCAO_PREVENTIVA);
    add(
        "Troca de Correia Dentada",
        "Substituição do kit de correia dentada e tensor",
        500.0,
        900.0,
        180,
        240,
        CategoriaServico.MANUTENCAO_PREVENTIVA);
    add(
        "Troca de Correia de Acessórios",
        "Substituição da correia do alternador/direção hidráulica",
        150.0,
        250.0,
        45,
        60,
        CategoriaServico.MANUTENCAO_PREVENTIVA);
    add(
        "Regulagem de Válvulas",
        "Ajuste técnico de folga de válvulas do motor",
        300.0,
        500.0,
        120,
        180,
        CategoriaServico.MANUTENCAO_PREVENTIVA);
    add(
        "Limpeza de Bicos Injetores",
        "Limpeza e equalização ultrassônica dos bicos",
        180.0,
        250.0,
        60,
        90,
        CategoriaServico.MANUTENCAO_PREVENTIVA);
    add(
        "Troca de Velas de Ignição",
        "Substituição do jogo de velas de ignição",
        120.0,
        200.0,
        40,
        60,
        CategoriaServico.MANUTENCAO_PREVENTIVA);
    add(
        "Troca de Cabos de Vela",
        "Substituição dos cabos de ignição ressecados",
        100.0,
        180.0,
        30,
        45,
        CategoriaServico.MANUTENCAO_PREVENTIVA);
    add(
        "Lubrificação de Suspensão",
        "Lubrificação de buchas, pivôs e articulações",
        80.0,
        120.0,
        40,
        60,
        CategoriaServico.MANUTENCAO_PREVENTIVA);
    add(
        "Reaperto Geral de Suspensão",
        "Verificação e reaperto de torque em parafusos da suspensão",
        100.0,
        150.0,
        60,
        90,
        CategoriaServico.MANUTENCAO_PREVENTIVA);
    add(
        "Check-up de Viagem",
        "Inspeção de 30 itens de segurança antes de viajar",
        150.0,
        250.0,
        60,
        90,
        CategoriaServico.MANUTENCAO_PREVENTIVA);

    // --- REPARO_MECANICO (25 items) ---
    add(
        "Troca de Pastilhas Dianteiras",
        "Substituição do par de pastilhas de freio dianteiras",
        150.0,
        250.0,
        45,
        60,
        CategoriaServico.REPARO_MECANICO);
    add(
        "Troca de Discos Dianteiros",
        "Substituição do par de discos de freio dianteiros",
        250.0,
        400.0,
        60,
        90,
        CategoriaServico.REPARO_MECANICO);
    add(
        "Troca de Amortecedores Dianteiros",
        "Substituição do par de amortecedores dianteiros",
        400.0,
        700.0,
        120,
        180,
        CategoriaServico.REPARO_MECANICO);
    add(
        "Troca de Embreagem",
        "Substituição do kit de embreagem (platô, disco e rolamento)",
        600.0,
        1200.0,
        240,
        360,
        CategoriaServico.REPARO_MECANICO);
    add(
        "Retífica de Cabeçote",
        "Serviço de retífica, plaina e assentamento de válvulas",
        1500.0,
        3000.0,
        480,
        960,
        CategoriaServico.REPARO_MECANICO);
    add(
        "Troca de Junta do Cabeçote",
        "Substituição da junta do cabeçote queimada",
        800.0,
        1500.0,
        300,
        480,
        CategoriaServico.REPARO_MECANICO);
    add(
        "Troca de Bomba d'Água",
        "Substituição da bomba de circulação de água do motor",
        250.0,
        450.0,
        90,
        150,
        CategoriaServico.REPARO_MECANICO);
    add(
        "Troca de Bomba de Combustível",
        "Substituição da bomba elétrica de combustível (refil)",
        300.0,
        500.0,
        60,
        120,
        CategoriaServico.REPARO_MECANICO);
    add(
        "Troca de Radiador",
        "Substituição de radiador com vazamento ou entupido",
        400.0,
        800.0,
        120,
        180,
        CategoriaServico.REPARO_MECANICO);
    add(
        "Troca de Escapamento Traseiro",
        "Substituição do silencioso traseiro",
        200.0,
        400.0,
        45,
        60,
        CategoriaServico.REPARO_MECANICO);
    add(
        "Troca de Catalisador",
        "Substituição do conversor catalítico (peça + mão de obra)",
        800.0,
        1500.0,
        60,
        120,
        CategoriaServico.REPARO_MECANICO);
    add(
        "Troca de Junta Homocinética",
        "Substituição da junta homocinética ou coifa rasgada",
        250.0,
        450.0,
        90,
        120,
        CategoriaServico.REPARO_MECANICO);
    add(
        "Troca de Rolamento de Roda",
        "Substituição de rolamento de roda ruidoso",
        150.0,
        300.0,
        60,
        90,
        CategoriaServico.REPARO_MECANICO);
    add(
        "Troca de Bandeja de Suspensão",
        "Substituição de bandeja com buchas estouradas",
        250.0,
        500.0,
        90,
        120,
        CategoriaServico.REPARO_MECANICO);
    add(
        "Troca de Terminal de Direção",
        "Substituição de terminal de direção com folga",
        100.0,
        200.0,
        45,
        60,
        CategoriaServico.REPARO_MECANICO);
    add(
        "Reparo de Caixa de Direção",
        "Manutenção e vedação em caixa de direção hidráulica",
        800.0,
        1800.0,
        240,
        480,
        CategoriaServico.REPARO_MECANICO);
    add(
        "Troca de Coxim do Motor",
        "Substituição de suporte do motor (coxim) quebrado",
        200.0,
        400.0,
        60,
        120,
        CategoriaServico.REPARO_MECANICO);
    add(
        "Descarbonização de Motor",
        "Limpeza interna de carbonização nas válvulas e pistões",
        400.0,
        700.0,
        180,
        300,
        CategoriaServico.REPARO_MECANICO);
    add(
        "Troca de Sonda Lambda",
        "Substituição de sensor de oxigênio defeituoso",
        250.0,
        500.0,
        45,
        60,
        CategoriaServico.REPARO_MECANICO);
    add(
        "Reparo de Câmbio Manual",
        "Manutenção interna de caixa de marchas (sincronizados)",
        1200.0,
        2500.0,
        480,
        960,
        CategoriaServico.REPARO_MECANICO);
    add(
        "Troca de Óleo Câmbio Automático",
        "Diálise e troca total de fluido ATF",
        800.0,
        1500.0,
        120,
        180,
        CategoriaServico.REPARO_MECANICO);
    add(
        "Troca de Válvula Termostática",
        "Substituição da válvula termostática travada",
        150.0,
        300.0,
        60,
        90,
        CategoriaServico.REPARO_MECANICO);
    add(
        "Retífica de Discos de Freio",
        "Passe em torno para regularizar superfície do disco",
        100.0,
        200.0,
        60,
        90,
        CategoriaServico.REPARO_MECANICO);
    add(
        "Troca de Cilindro Mestre",
        "Substituição do cilindro mestre de freio com vazamento",
        300.0,
        600.0,
        90,
        150,
        CategoriaServico.REPARO_MECANICO);
    add(
        "Sangria de Freio",
        "Remoção de ar do sistema hidráulico de freios",
        100.0,
        150.0,
        45,
        60,
        CategoriaServico.REPARO_MECANICO);

    // --- ELETRICA (15 items) ---
    add(
        "Troca de Bateria",
        "Instalação e teste de carga de bateria nova",
        50.0,
        100.0,
        20,
        30,
        CategoriaServico.ELETRICA);
    add(
        "Reparo de Alternador",
        "Troca de escovas, rolamentos ou regulador de voltagem",
        250.0,
        500.0,
        120,
        180,
        CategoriaServico.ELETRICA);
    add(
        "Reparo de Motor de Arranque",
        "Manutenção em motor de partida (bendix, automático)",
        200.0,
        400.0,
        120,
        180,
        CategoriaServico.ELETRICA);
    add(
        "Troca de Lâmpadas de Farol",
        "Substituição de par de lâmpadas halógenas queimadas",
        40.0,
        80.0,
        20,
        30,
        CategoriaServico.ELETRICA);
    add(
        "Instalação de Kit Multimídia",
        "Instalação de central multimídia 2DIN e câmera de ré",
        300.0,
        600.0,
        180,
        300,
        CategoriaServico.ELETRICA);
    add(
        "Reparo de Vidro Elétrico",
        "Conserto de máquina de vidro ou botões de comando",
        150.0,
        300.0,
        90,
        120,
        CategoriaServico.ELETRICA);
    add(
        "Troca de Fusíveis",
        "Identificação e troca de fusível queimado",
        30.0,
        50.0,
        15,
        30,
        CategoriaServico.ELETRICA);
    add(
        "Instalação de Sensor de Ré",
        "Instalação de sensores de estacionamento no para-choque",
        200.0,
        400.0,
        120,
        180,
        CategoriaServico.ELETRICA);
    add(
        "Carga de Gás Ar Condicionado",
        "Recarga de gás refrigerante R134a e teste de vazamento",
        150.0,
        250.0,
        45,
        60,
        CategoriaServico.ELETRICA);
    add(
        "Reparo de Trava Elétrica",
        "Conserto de atuador de trava de porta",
        100.0,
        200.0,
        60,
        90,
        CategoriaServico.ELETRICA);
    add(
        "Instalação de Alarme",
        "Instalação de sistema de alarme com sensor de presença",
        250.0,
        500.0,
        180,
        240,
        CategoriaServico.ELETRICA);
    add(
        "Troca de Bobina de Ignição",
        "Substituição de bobina de ignição com falha",
        150.0,
        300.0,
        30,
        60,
        CategoriaServico.ELETRICA);
    add(
        "Diagnóstico de Fuga de Corrente",
        "Identificação de consumo parasita que descarrega bateria",
        150.0,
        300.0,
        60,
        180,
        CategoriaServico.ELETRICA);
    add(
        "Instalação de Farol de Milha",
        "Instalação de kit de faróis auxiliares de neblina",
        200.0,
        400.0,
        120,
        180,
        CategoriaServico.ELETRICA);
    add(
        "Reparo de Painel de Instrumentos",
        "Conserto de velocímetro, conta-giros ou marcadores",
        300.0,
        600.0,
        180,
        300,
        CategoriaServico.ELETRICA);

    // --- DIAGNOSTICO (10 items) ---
    add(
        "Diagnóstico com Scanner",
        "Leitura e interpretação de códigos de falha da ECU",
        100.0,
        200.0,
        30,
        60,
        CategoriaServico.DIAGNOSTICO);
    add(
        "Inspeção Pré-Compra",
        "Avaliação mecânica completa de veículo usado",
        300.0,
        500.0,
        120,
        180,
        CategoriaServico.DIAGNOSTICO);
    add(
        "Teste de Compressão de Motor",
        "Medição de compressão dos cilindros do motor",
        150.0,
        250.0,
        60,
        90,
        CategoriaServico.DIAGNOSTICO);
    add(
        "Teste de Arrefecimento",
        "Teste de estanqueidade e pressão do sistema de água",
        80.0,
        150.0,
        30,
        60,
        CategoriaServico.DIAGNOSTICO);
    add(
        "Diagnóstico de Ruídos",
        "Localização de barulhos na suspensão ou carroceria",
        100.0,
        200.0,
        60,
        120,
        CategoriaServico.DIAGNOSTICO);
    add(
        "Análise de Emissões",
        "Teste de emissões de poluentes com analisador de gases",
        100.0,
        150.0,
        30,
        45,
        CategoriaServico.DIAGNOSTICO);
    add(
        "Teste de Bateria e Alternador",
        "Avaliação digital do sistema de carga e partida",
        50.0,
        80.0,
        15,
        30,
        CategoriaServico.DIAGNOSTICO);
    add(
        "Diagnóstico de Injeção",
        "Análise aprofundada de falhas de injeção eletrônica",
        200.0,
        400.0,
        120,
        240,
        CategoriaServico.DIAGNOSTICO);
    add(
        "Vistoria Cautelar",
        "Verificação estrutural e documental do veículo",
        250.0,
        400.0,
        90,
        120,
        CategoriaServico.DIAGNOSTICO);
    add(
        "Teste de Pressão de Combustível",
        "Verificação da pressão da bomba e linha de alimentação",
        100.0,
        180.0,
        45,
        60,
        CategoriaServico.DIAGNOSTICO);

    // --- ESTETICA (15 items) ---
    add(
        "Lavagem Simples",
        "Lavagem externa e aspiração interna básica",
        40.0,
        70.0,
        40,
        60,
        CategoriaServico.ESTETICA);
    add(
        "Lavagem Completa com Cera",
        "Lavagem detalhada, pretinho e aplicação de cera líquida",
        80.0,
        120.0,
        90,
        120,
        CategoriaServico.ESTETICA);
    add(
        "Polimento Técnico",
        "Correção de pintura e remoção de riscos superficiais",
        400.0,
        800.0,
        240,
        480,
        CategoriaServico.ESTETICA);
    add(
        "Cristalização de Pintura",
        "Proteção e brilho intenso para a pintura (6 meses)",
        250.0,
        400.0,
        180,
        300,
        CategoriaServico.ESTETICA);
    add(
        "Vitrificação de Pintura",
        "Proteção cerâmica de longa duração (até 3 anos)",
        800.0,
        1500.0,
        360,
        600,
        CategoriaServico.ESTETICA);
    add(
        "Higienização Interna",
        "Limpeza profunda de bancos, carpetes e teto",
        300.0,
        500.0,
        240,
        360,
        CategoriaServico.ESTETICA);
    add(
        "Hidratação de Couro",
        "Limpeza e hidratação de bancos de couro",
        150.0,
        250.0,
        90,
        120,
        CategoriaServico.ESTETICA);
    add(
        "Lavagem Técnica de Motor",
        "Limpeza segura do cofre do motor com produtos específicos",
        100.0,
        180.0,
        60,
        90,
        CategoriaServico.ESTETICA);
    add(
        "Polimento de Faróis",
        "Restauração da transparência das lentes dos faróis",
        100.0,
        200.0,
        60,
        90,
        CategoriaServico.ESTETICA);
    add(
        "Oxi-Sanitização",
        "Esterilização da cabine com ozônio (elimina odores)",
        80.0,
        150.0,
        30,
        60,
        CategoriaServico.ESTETICA);
    add(
        "Martelinho de Ouro",
        "Remoção de pequenos amassados sem necessidade de pintura",
        150.0,
        500.0,
        60,
        240,
        CategoriaServico.ESTETICA);
    add(
        "Impermeabilização de Bancos",
        "Proteção contra líquidos em tecidos",
        200.0,
        350.0,
        90,
        120,
        CategoriaServico.ESTETICA);
    add(
        "Revitalização de Plásticos",
        "Tratamento de plásticos externos ressecados",
        80.0,
        150.0,
        45,
        60,
        CategoriaServico.ESTETICA);
    add(
        "Descontaminação de Pintura",
        "Remoção de impurezas incrustadas com clay bar",
        120.0,
        200.0,
        90,
        120,
        CategoriaServico.ESTETICA);
    add(
        "Limpeza de Chassis",
        "Lavagem detalhada por baixo do veículo",
        80.0,
        150.0,
        45,
        60,
        CategoriaServico.ESTETICA);

    // --- OUTROS (15 items) ---
    add(
        "Instalação de Insulfilm",
        "Aplicação de película de controle solar",
        150.0,
        400.0,
        90,
        180,
        CategoriaServico.OUTROS);
    add(
        "Instalação de Engate",
        "Colocação de engate para reboque (homologado)",
        300.0,
        600.0,
        60,
        120,
        CategoriaServico.OUTROS);
    add(
        "Instalação de Rack de Teto",
        "Montagem de barras transversais ou bagageiro",
        100.0,
        200.0,
        45,
        60,
        CategoriaServico.OUTROS);
    add(
        "Serviço de Guincho",
        "Remoção de veículo avariado (raio urbano)",
        200.0,
        400.0,
        60,
        120,
        CategoriaServico.OUTROS);
    add(
        "Armazenamento de Pneus",
        "Guarda de pneus extras/inverno (custo mensal)",
        50.0,
        100.0,
        0,
        0,
        CategoriaServico.OUTROS);
    add(
        "Serviço de Despachante",
        "Assessoria para regularização documental",
        150.0,
        300.0,
        0,
        0,
        CategoriaServico.OUTROS);
    add(
        "Emplacamento",
        "Fixação e lacração de placas Mercosul",
        100.0,
        200.0,
        30,
        60,
        CategoriaServico.OUTROS);
    add(
        "Curso Básico de Mecânica",
        "Workshop de manutenção básica para proprietários",
        200.0,
        500.0,
        240,
        480,
        CategoriaServico.OUTROS);
    add(
        "Aluguel de Carro Reserva",
        "Diária de veículo popular enquanto o carro conserta",
        100.0,
        150.0,
        0,
        0,
        CategoriaServico.OUTROS);
    add(
        "Instalação de Bloqueador",
        "Sistema antifurto com corte de combustível",
        200.0,
        350.0,
        90,
        120,
        CategoriaServico.OUTROS);
    add(
        "Gravação de Chassis",
        "Gravação do número do chassi nos vidros (antifurto)",
        80.0,
        150.0,
        30,
        45,
        CategoriaServico.OUTROS);
    add(
        "Adaptação para PCD",
        "Instalação de pomo de volante ou acelerador manual",
        500.0,
        1500.0,
        240,
        480,
        CategoriaServico.OUTROS);
    add(
        "Blindagem de Pneus",
        "Aplicação de vacina antifuro nos pneus",
        100.0,
        200.0,
        30,
        60,
        CategoriaServico.OUTROS);
    add(
        "Restauração de Rodas",
        "Pintura, diamantação e desamasso de rodas",
        400.0,
        800.0,
        240,
        480,
        CategoriaServico.OUTROS);
    add(
        "Conversão para GNV",
        "Instalação e homologação de kit gás natural 5ª geração",
        2500.0,
        4000.0,
        480,
        960,
        CategoriaServico.OUTROS);
  }

  private static void add(
      String nome,
      String desc,
      double minPrice,
      double maxPrice,
      int minTime,
      int maxTime,
      CategoriaServico cat) {
    TEMPLATES.add(new ServicoTemplate(nome, desc, minPrice, maxPrice, minTime, maxTime, cat));
  }

  @Override
  public Servico create() {
    if (templateQueue.isEmpty()) {
      templateQueue.addAll(TEMPLATES);
      // Shuffle to ensure random order if we seed less than total,
      // but keeps uniqueness within a cycle of 100
      Collections.shuffle((List) templateQueue);
    }

    ServicoTemplate t = templateQueue.poll();

    // Calculate random price within range
    double priceVal = t.minPrice + (Math.random() * (t.maxPrice - t.minPrice));
    BigDecimal price = BigDecimal.valueOf(priceVal).setScale(2, RoundingMode.HALF_UP);

    // Calculate random time within range
    long timeVal = t.minTime + (long) (Math.random() * (t.maxTime - t.minTime));
    // Ensure at least 1 minute if range is 0
    if (timeVal <= 0) {
      timeVal = 30;
    }

    return new Servico(t.nome, t.desc, price, Duration.ofMinutes(timeVal), t.cat);
  }

  private record ServicoTemplate(
      String nome,
      String desc,
      double minPrice,
      double maxPrice,
      int minTime,
      int maxTime,
      CategoriaServico cat) {}
}
