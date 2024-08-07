type pd_val = { declare : Declare.t; ltl : Ltl.term }

type prob_declare =
  | PROB_DECLARE of {
      crisps : pd_val list;
      probabilities : float * pd_val list;
    }
