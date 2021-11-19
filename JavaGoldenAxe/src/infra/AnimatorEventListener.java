package infra;

import infra.Animator.AnimatorEvent;

/**
 * AnimatorListener class.
 * 
 * @author Leonardo Ono (ono.leo80@gmail.com)
 */
public interface AnimatorEventListener {

    public void onAnimatorEventTriggered(AnimatorEvent event);
    
}
