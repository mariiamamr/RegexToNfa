//package com.company;
import java.io.FileWriter;
import java.util.Scanner;
import java.util.ArrayList;
import java.util.Stack;
import org.json.simple.JSONObject;
import org.json.simple.JSONArray;
public class Thompson {
    public static class changes {
        public int state_from, state_to;
        public char symbols;

        //The Constructor
        public changes(int in1, int in2, char symbol) {
            this.state_from = in1;
            this.state_to = in2;
            this.symbols = symbol;
        }
    }
    public static class NFA {
        FileWriter file;
        int startingState;
        int TerminatingState;
        public ArrayList<Integer> states;
        public ArrayList<changes> transitions;
        public int final_state;

        public NFA() {
            this.TerminatingState = 0;
            this.startingState = 0;
            this.states = new ArrayList<Integer>();
            this.transitions = new ArrayList<changes>();
            this.final_state = 0;
        }

        public NFA(int n) {
            this.startingState = 0;
            this.states = new ArrayList<Integer>();
            this.transitions = new ArrayList<changes>();
            this.final_state = 0;
            this.addStates(n);
            this.TerminatingState = n - 1;//////et2kdy ya zefta w sheleha?????????
        }

        public NFA(char R) {
            this.states = new ArrayList<Integer>();
            this.addStates(2);
            this.final_state = 1;
            this.transitions = new ArrayList<changes>();
            this.transitions.add(new changes(0, 1, R));
        }

        public boolean isEmpty(){
            if(this.final_state==0) return true;
            return false;
        }
        public void addStates(int size) {
            for (int i = 0; i < size; i++)
                this.states.add(i);
        }

        public void Print() {
            StringBuilder sb = new StringBuilder();
            for (changes t : transitions) {
                System.out.println("(" + t.state_from + ", " + t.symbols +
                        ", " + t.state_to + ")");
            }
            try{
                file=new FileWriter("nfa.json"); // make it .json b3d kda???????????????
            }
            catch(Exception e){}
            JSONArray arr=new JSONArray();

            int max = 0;
            for (changes t : transitions) {
                if(t.state_to>max){
                    max=t.state_to;
                }
            }
            JSONObject obj_end = new JSONObject();
            for (int i = 0; i <=max; i++) {
                JSONObject obj = new JSONObject();
                JSONObject obj2 = new JSONObject();
                if(i == 0){obj.put("startingState","S"+i);}
                for (changes t : transitions) {
                    if (t.state_from == i) {
                        if(i == max){obj.put("isTerminatingState","true");}
                        if(i != max){obj.put("isTerminatingState","false");}
                        obj.put(t.symbols,"S"+ t.state_to);
                        obj2.put("S"+ i,obj );

                        try{
                            file.write(obj2.toJSONString());
                        }
                        catch(Exception e){}
                        //System.out.println(obj);
                    }
                }
                //System.out.println("test");
            }
            obj_end.put("S"+max,"isTerminatingState:true");
            try{
                file.write(obj_end.toJSONString());
            }
            catch(Exception e){}
            try{
                file.close();
            }
            catch(Exception e){}
        }
    }/////////////////////////////////////////////////End NFA

    /* OR -> a|b a+b  */
    public static NFA OR(NFA first, NFA second) {

        int size1 = first.states.size();
        int size2 = second.states.size();
        //new size=size of first branch + size of second branch + 2 (two epsilon)(one at first and one at end)
        int newSize = size1 + size2 + 2;
        int newFinalState = first.states.size() + second.states.size() + 1; // end first NFA
        NFA newNFA = new NFA(newSize);
        newNFA.final_state = newFinalState;
        changes FirstBranch = new changes(0, 1, 'E'); //from zero state to first branch
        changes SecondBranch = new changes(0, size1 + 1, 'E'); //from zero state to second branch
        newNFA.transitions.add(FirstBranch);
        newNFA.transitions.add(SecondBranch);
        for (changes t : first.transitions) // transitions of first NFA
        {
            int newFromState = t.state_from + 1;
            int newToState = t.state_to + 1;
            changes newTrans = new changes(newFromState, newToState, t.symbols);
            newNFA.transitions.add(newTrans);
        }
        for (changes t : second.transitions) // transitions of second NFA
        {
            int newFromState = t.state_from + first.states.size() + 1;
            int newToState = t.state_to + first.states.size() + 1;
            changes newTrans = new changes(newFromState, newToState, t.symbols);
            newNFA.transitions.add(newTrans);
        }
        int lastState1 = first.states.size(); // end first NFA (first branch)
        changes newLastTrans1 = new changes(lastState1, newFinalState, 'E');
        newNFA.transitions.add(newLastTrans1);
        int lastState2 = second.states.size() + first.states.size(); // end second NFA (Second branch)
        changes newLastTrans2 = new changes(lastState2, newFinalState, 'E');
        newNFA.transitions.add(newLastTrans2);
        return newNFA;

    }

    /*
     * Repetitions zero ot more times *
     * */
    public static NFA Star(NFA A) {
        int newSize = A.states.size() + 2;
        NFA newNFA = new NFA(newSize);
        newNFA.final_state = A.final_state + 1;
        changes firstTrans = new changes(0, 1, 'E');
        newNFA.transitions.add(firstTrans);
        for (changes t : A.transitions) {
            int newFromState = t.state_from + 1;
            int newToState = t.state_to + 1;
            changes newTrans = new changes(newFromState, newToState, t.symbols);
            newNFA.transitions.add(newTrans);
        }
        int finalStateA = A.final_state + 1;
        int newFinalStateA = A.final_state + 2;
        changes newTrans1 = new changes(finalStateA, newFinalStateA, 'E');
        changes newTrans2 = new changes(finalStateA, 1, 'E');
        changes newTrans3 = new changes(0, newFinalStateA, 'E');
        newNFA.transitions.add(newTrans1);
        newNFA.transitions.add(newTrans2);
        newNFA.transitions.add(newTrans3);
        return newNFA;
    }

    /*
        Concatenation
    */

    public static NFA Concatenation(NFA first, NFA second) {
        second.states.remove(0);
        for (changes t : second.transitions) {
            int a1 = t.state_from;
            int a2 = first.states.size();
            int newFromState = a1 + a2 - 1;
            int b1 = t.state_to;
            int b2 = first.states.size();
            int newToState = b1 + b2 - 1;
            changes newTrans = new changes(newFromState, newToState, t.symbols);
            first.transitions.add(newTrans);
        }
        for(Integer number:second.states)
        {
            int m = first.states.size();
            int newNumber = number + m + 1;
            first.states.add(newNumber);
        }
        first.final_state = first.states.size() + second.states.size() -2 ;
        return first;
    }



    // validRegEx() - checks if given string is a valid regular expression.
    public static boolean validRegEx(String exp) {

        /* If Expression empty */
        if (exp.isEmpty()) {
            System.out.println("the regex expression is empty!");
            return false;
        }
        /* Expression must have letters and operators only */
        for (char c : exp.toCharArray()) {
            int a = c - '0';
            if (((c >= 'a' && c <= 'z') || (c == 'E') || (a >= 0 && a <= 9) ||
                    (c == '(' || c == ')' || c == '*' || c == '|' || c == '+')) == false)
                return false;
        }
        /* If Expression has '**' or '||' */
        Boolean concat = false;
        Boolean or = false;
        for (char c : exp.toCharArray()) {
            if (c == '*') {
                if (concat == true)
                    return false;
                else {
                    concat = true;
                    or = false;
                }
            } else if (c == '|') {
                if (or == true)
                    return false;
                else {
                    or = true;
                    concat = false;
                }
            } else {
                or = false;
                concat = false;
            }
        }
        /* Expression must have open brackets = closed brackets */
        int numOpen = 0;
        Boolean ErrorBrackets = false;
        for (char c : exp.toCharArray()) {
            if (c == ')') {
                if (numOpen > 0)
                    numOpen--;
                else {
                    ErrorBrackets = true;
                    break;
                }
            }
            if (c == '(')
                numOpen++;
        }
        if (or == false && ErrorBrackets == false && numOpen == 0)
            return true;
        else
            return false;
    }

    public static NFA doWork(String regex) {
        int len = regex.length();
        Stack <Character> methods = new Stack <Character>(); //operators
        Stack <NFA> inputs = new Stack <NFA>(); // operands
        Stack <NFA> inputsConcat = new Stack <NFA>();
        Boolean concat = false;// int noBrackets = 0;

        for(int i=0;i<len;i++)
        {
            char a = regex.charAt(i);
            int aa = a-'0';
            if( (a >= 'a'&& a <= 'z') || (aa >= 0 && aa <= 9) || (a == 'E'))  //character
            {
                if(!concat)
                {
                    concat = true;
                    NFA A = new NFA(a);
                    inputs.push(A);
                }
                else
                {
                    methods.push('.');
                    NFA A = new NFA(a);
                    inputs.push(A);
                }
            }
            else if( a == '|' || a == '+') // OR
            {
                concat = false;
                methods.push('|');
            }
            else if( a == '*') // Repetition
            {
                concat = true;
                NFA toRe = inputs.pop();
                NFA result = Star(toRe);
                inputs.push(result);
            }
            else if( a == '(')
            {
                //noBrackets = noBrackets + 1;
                methods.push('(');
                concat = false;
            }
            else if(a == ')')
            {
                while(!methods.isEmpty() && methods.peek()!= '(')
                {
                    char m = methods.pop();
                    if(m == '+' || m == '|')
                    {
                        NFA first;
                        NFA second = inputs.pop();
                        if(!methods.empty() && methods.peek() == '.')
                        {
                            NFA c = inputs.pop();
                            inputsConcat.push(c);
                            while(!methods.empty())
                            {
                                if(methods.peek()=='.')
                                {
                                    NFA c1 = inputs.pop();
                                    inputsConcat.push(c1);
                                    methods.pop();
                                }
                                else
                                    break;
                            }
                            NFA n1 = inputsConcat.pop();
                            NFA n2 = inputsConcat.pop();////////////////////////
                            first = Concatenation(n1,n2);
                            if(inputsConcat.size()>0)
                            {
                                while(inputsConcat.size()>0)
                                {
                                    NFA n3 = inputsConcat.pop();
                                    first = Concatenation(first,n3);
                                }
                            }
                            NFA result = OR(first,second);
                            inputs.push(result);
                        }
                        else
                        {
                            first = inputs.pop();
                            NFA result = OR(first,second);
                            inputs.push(result);
                        }
                    }
                    else if (m == '.')
                    {
                        NFA second = inputs.pop();
                        NFA first= inputs.pop();
                        NFA result1 = Concatenation(first,second);
                        inputs.push(result1);
                    }
                }
                concat = false;
            }
        }
        if(!methods.empty() && inputs.empty())
        {
            System.out.println("Error in input expression");
            System.exit(0);
        }
        while(!methods.empty())
        {
            if(inputs.empty())
            {
                System.out.println("Error in input expression");
                System.exit(0);
            }
            char m1 = methods.pop();
            if(m1 == '*')
            {
                NFA re1 = inputs.pop();
                NFA re = Star(re1);
                inputs.push(re);
            }
            else if(m1 == '+' || m1 == '|')
            {
                NFA first2;
                NFA second2 = inputs.pop();
                if(!methods.empty() && methods.peek()=='.')
                {
                    NFA in = inputs.pop();
                    inputsConcat.push(in);
                    while(!methods.empty() && methods.peek()=='.')
                    {
                        methods.pop();
                        NFA in2 = inputs.pop();
                        inputsConcat.push(in2);
                    }
                    NFA a1 = inputsConcat.pop();
                    NFA a2 = inputsConcat.pop();
                    first2 = Concatenation(a1,a2);
                    while(!inputsConcat.empty())
                    {
                        NFA a3 = inputsConcat.pop();
                        first2 = Concatenation(first2,a3);
                    }
                    NFA result2 = OR(first2,second2);
                    inputs.push(result2);
                }
                else
                {
                    first2=inputs.pop();
                    NFA result2 = OR(first2,second2);
                    inputs.push(result2);
                }
            }
            else if(m1 == '.')
            {
                NFA b1 = inputs.pop();
                NFA b2 = inputs.pop();
                NFA b3 = Concatenation(b2,b1);
                inputs.push(b3);
            }
        }
        NFA finalNFA = inputs.pop();
        return finalNFA;

    }

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        String regex;
        System.out.println("\nEnter a regular expression");
        while (sc.hasNextLine()) {
            regex = sc.nextLine();
            Boolean validated = validRegEx(regex) ;
            if(!validated)
            {
                System.out.println("Error in input regex Expression...");
                System.exit(0);
            }
            NFA nfa_out = doWork(regex);
            if(nfa_out.isEmpty()){
                System.out.println("\nNothing to show..Stopped!");
            }
            else {
                nfa_out.Print();
                System.out.println("\nOpen the JSON file to see the output..");
            }
        }
    }
}

