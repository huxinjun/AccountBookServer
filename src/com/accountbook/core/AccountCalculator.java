package com.accountbook.core;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import com.accountbook.model.Account;
import com.accountbook.model.Member;
import com.accountbook.model.PayResult;
import com.accountbook.model.PayTarget;

/**
 * 账户结算
 * Created by xinjun on 2017/8/1 14:46
 */
public class AccountCalculator {

    private Account mAccount;

    public AccountCalculator(Account account) {
        this.mAccount=account;
    }
    
    public void setAccount(Account account){
    	this.mAccount=account;
    }
    

    /**
     * 计算所有可能的支付结果
     * @param allPaidIn 
     * @return
     */
    public List<PayResult> calc(float allPaidIn) throws CalculatorException{
        mAccount.setPayResult(new ArrayList<PayResult>());

        /**
         * 0.算出总花费
         * 1.算出所有参与者应缴金额
         * 2.排除含有特殊缴费规则的用户后其余用户AA制,并算出AA制平均缴费额度
         * 3.按照平均缴费分组,大于平均的一组,小于平均的一组
         * 4.大于平均的向小于平均的收费,直到收到多余平均值得金额后换下一个大于平均值的参与者,直到所有大于平均值得参与者收到自己多交的那部分钱
         */
        //======================step0=========================
        //======================step1=========================
        calcShouldPay(mAccount.getMembers(), allPaidIn);


        //======================step3=========================
        List<Member> IN=new ArrayList<>();//需要收钱的人
        List<Member> OUT=new ArrayList<>();//需要支出的人

        for (Member p:mAccount.getMembers()) {
            //实际支持少余平均值,需要付钱
            if(p.getPaidIn()<p.getShouldPay())
                OUT.add(p);
            //实际支持多余平均值,需要收钱
            else if(p.getPaidIn()>p.getShouldPay())
                IN.add(p);
        }

        System.out.println("IN:"+IN.toString());
        System.out.println("OUT:"+OUT.toString());

        //======================step4=========================
        //换位置后再计算结果
        for(int m=0;m<OUT.size()-1;m++)
            for(int n=m;n<OUT.size();n++){
                transposition(OUT,n,m);
                PayResult calcResult = calcResult(IN,OUT);
                mAccount.getPayResult().add(calcResult);
                System.out.println("调换位置");
            }
        if(mAccount.getPayResult().size()==0){
        	PayResult calcResult = calcResult(IN,OUT);
        	mAccount.getPayResult().add(calcResult);
        }
        //======================done===========================
        return mAccount.getPayResult();
    }

    /**
     * 计算付款着和收款者整个流程
     * @param IN 收款者
     * @param OUT 付款者
     */
    public PayResult calcResult(List<Member> IN,List<Member> OUT){
        PayResult result=new PayResult();
        for(int i=0;i<IN.size();i++){
            //当前收钱的人
            Member IN_P = IN.get(i);
            int n=0;
            //确定付款人的位置,n之前的人都已经付过了
            while (n<OUT.size() && OUT.get(n).getCalcData()>=OUT.get(n).getShouldPay())
                n++;
            Member OUT_P;
            while (n<OUT.size()){
                OUT_P = OUT.get(n++);

                PayTarget payTarget=new PayTarget();
                payTarget.setPaidId(OUT_P.getMemberId());
                payTarget.setReceiptId(IN_P.getMemberId());

                double needMoney=IN_P.getPaidIn()-(IN_P.getShouldPay()+IN_P.getCalcData());//还需收的钱.
                if(needMoney==0)
                    break;
                if(needMoney>=OUT_P.getShouldPay()-(OUT_P.getPaidIn()+OUT_P.getCalcData())){
                    //收钱的人多付的钱大于付钱的人还需付的钱,把付钱人还需付的钱全部收来
                    double pay=OUT_P.getShouldPay()-(OUT_P.getPaidIn()+OUT_P.getCalcData());
                    OUT_P.setCalcData(OUT_P.getShouldPay());
                    IN_P.setCalcData((float) (IN_P.getCalcData()+pay));
                    payTarget.setMoney((float) round(pay));
                  //初始值为全款未付
                    payTarget.setWaitPaidMoney(payTarget.getMoney());

                }else if(needMoney<OUT_P.getShouldPay()-(OUT_P.getPaidIn()+OUT_P.getCalcData())){
                    //收钱人多付的钱少于付钱人还需付的钱,向付钱人收自己多付的那部分
                    double pay=needMoney;
                    OUT_P.setCalcData((float) (OUT_P.getCalcData()+pay));
                    IN_P.setCalcData((float) (IN_P.getCalcData()+pay));
                    payTarget.setMoney((float) round(pay));
                  //初始值为全款未付
                    payTarget.setWaitPaidMoney(payTarget.getMoney());
                }
                if(result.getPayTarget()==null)
                	result.setPayTarget(new ArrayList<PayTarget>());
                result.getPayTarget().add(payTarget);
            }
        }
        //初始化
        for (Member p:IN)
            p.setCalcData(0);
        for (Member p:OUT)
            p.setCalcData(0);
        return result;
    }
    
    
    /**
     * 计算每个人的应付款
     * @param members 人
     * @param paidIn 总花费
     */
    public void calcShouldPay(List<Member> members,float paidIn) throws CalculatorException{
    	if(paidIn!=0)
    		mAccount.setPaidIn(paidIn);
    	else{
    		float allPaidIn=0;
	        for (Member p:mAccount.getMembers())
	        	allPaidIn+=p.getPaidIn();
	         mAccount.setPaidIn(allPaidIn);
    	}
    	System.out.println("总支付:"+mAccount.getPaidIn());
    	
    	double hasRuleMoney=0;
        int noRulePersonCount=0;
        for (Member p:mAccount.getMembers()) {
            if(p.getRuleType()==0) {
                noRulePersonCount++;
                //排除总金额中有些人给自己花了一部分钱,别人无需AA支付这个钱
                p.setShouldPay(fixed2(p.getMoneyForSelf()));
                hasRuleMoney+=p.getMoneyForSelf();
                continue;
            }
            //number_type   0:基于总支出的百分比的值  1:缴费为一个固定值
            switch (p.getRuleType()){
                case Member.RULE_TYPE_PERCENT:
                    if(p.getRuleNum()>100)
                        throw new CalculatorException("您为"+p.getMemberName()+"设置的支付类型是基于总支出百分比的值,取值范围[0~100]!");
                    //应缴金额
                    p.setShouldPay(fixed2(mAccount.getPaidIn()*(p.getRuleNum()/100)));
                    break;
                case Member.RULE_TYPE_NUMBER:
                    if(p.getRuleNum()>mAccount.getPaidIn())
                        throw new CalculatorException(p.getMemberName()+"支付的金额超过总支出!");
                    p.setShouldPay(fixed2(p.getRuleNum()));
                    break;
            }
            hasRuleMoney+=p.getShouldPay();
        }

        //======================step2=========================
        float averageMoney=(float) ((mAccount.getPaidIn()-hasRuleMoney)/noRulePersonCount);
        System.out.println("平均消费:"+averageMoney);
        
        for (Member p:mAccount.getMembers()) {
            if(p.getRuleType()==0) {
                //没有设置规则用户需要支付的金额
                p.setShouldPay(fixed2(averageMoney+p.getShouldPay()));
                System.out.println(p.getMemberName()+"保留小数:"+p.getShouldPay());
            }
        }
        
    }
    
    public static float fixed2(double num){
        BigDecimal bd = new BigDecimal(num);   
        float result = bd.setScale(2,BigDecimal.ROUND_HALF_UP).floatValue();
        return result;
    }
    
    

    /**
     * 数组元素换位置
     * @param target 数组
     * @param i 位置
     * @param j 位置
     */
    private void transposition(List<Member> target,int i,int j){
        if(i==j)
            return;
        Member temp=target.get(i);
        target.set(i,target.get(j));
        target.set(j,temp);
    }

    public double round(double value) {
        BigDecimal bd = new BigDecimal(value);
        bd = bd.setScale(3, BigDecimal.ROUND_HALF_DOWN);
        double d = bd.doubleValue();
        bd = null;
        return d;
    }


    /**
     * 计算异常
     * Created by xinjun on 2017/8/1 20:58
     */
    public class CalculatorException extends Exception{

        /**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		/**
         * Constructs a new exception with the specified detail message.  The
         * cause is not initialized, and may subsequently be initialized by
         * a call to {@link #initCause}.
         *
         * @param message the detail message. The detail message is saved for
         *                later retrieval by the {@link #getMessage()} method.
         */
        public CalculatorException(String message) {
            super(message);
        }
    }


}
