package org.example;

public final class EnumTypes {

    public enum EnumStatoContratto {
        ATTIVO,
        SCADUTO,
        AMBIGUO,
        SOSPESO,
        STORNATO,
    }

    public enum EnumContratto {
        IDULTIMOMOVERRATO,
        DASTORNARE,
        CROSSCOMPAGNIA,
        SCARTIDARECUPERARE,
        NESSUNCONTRATTO,
        COMPAGNIANONGESTITA

    }

    public enum EnumStatoPeriferica {
        ATTIVA,
        ABBINATA,
        DISATTIVA,
        DISINSTALLATA,
        RIENTRATA,
        INSOSTITUZIONE,
        AMBIGUA,
    }

    public enum EnumPeriferica {
        PERIFERICASUDIVERSOCONTRATTO,
        NESSUNAPERIFERICA,
        SANAREEXT,
        DOPPIAA,
        NODATE,
    }

    public enum EnumTipoMovimento {
        EMISSIONE,
        MODIFICA,
        RINNOVO,
        STORNO
    }

    public enum EnumStatoPolizza {
        EMISSIONE,
        MODIFICA,
        RINNOVO,
        STORNO,
        INCORSO,
        RIAMMISSIONE,
        CASENONIDENTIFICATO,
        SOSPESA,
        AMBIGUO
    }
}
