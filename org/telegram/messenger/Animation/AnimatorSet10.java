package org.telegram.messenger.Animation;

import android.view.animation.Interpolator;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import org.telegram.messenger.Animation.Animator10.AnimatorListener;

public final class AnimatorSet10 extends Animator10 {
    private ValueAnimator mDelayAnim = null;
    private long mDuration = -1;
    private Interpolator mInterpolator = null;
    private boolean mNeedsSort = true;
    private HashMap<Animator10, Node> mNodeMap = new HashMap();
    private ArrayList<Node> mNodes = new ArrayList();
    private ArrayList<Animator10> mPlayingSet = new ArrayList();
    private AnimatorSetListener mSetListener = null;
    private ArrayList<Node> mSortedNodes = new ArrayList();
    private long mStartDelay = 0;
    private boolean mStarted = false;
    boolean mTerminated = false;

    public class Builder {
        private Node mCurrentNode;

        Builder(Animator10 anim) {
            this.mCurrentNode = (Node) AnimatorSet10.this.mNodeMap.get(anim);
            if (this.mCurrentNode == null) {
                this.mCurrentNode = new Node(anim);
                AnimatorSet10.this.mNodeMap.put(anim, this.mCurrentNode);
                AnimatorSet10.this.mNodes.add(this.mCurrentNode);
            }
        }

        public Builder with(Animator10 anim) {
            Node node = (Node) AnimatorSet10.this.mNodeMap.get(anim);
            if (node == null) {
                node = new Node(anim);
                AnimatorSet10.this.mNodeMap.put(anim, node);
                AnimatorSet10.this.mNodes.add(node);
            }
            node.addDependency(new Dependency(this.mCurrentNode, 0));
            return this;
        }

        public Builder before(Animator10 anim) {
            Node node = (Node) AnimatorSet10.this.mNodeMap.get(anim);
            if (node == null) {
                node = new Node(anim);
                AnimatorSet10.this.mNodeMap.put(anim, node);
                AnimatorSet10.this.mNodes.add(node);
            }
            node.addDependency(new Dependency(this.mCurrentNode, 1));
            return this;
        }

        public Builder after(Animator10 anim) {
            Node node = (Node) AnimatorSet10.this.mNodeMap.get(anim);
            if (node == null) {
                node = new Node(anim);
                AnimatorSet10.this.mNodeMap.put(anim, node);
                AnimatorSet10.this.mNodes.add(node);
            }
            this.mCurrentNode.addDependency(new Dependency(node, 1));
            return this;
        }

        public Builder after(long delay) {
            Animator10 anim = ValueAnimator.ofFloat(0.0f, 1.0f);
            anim.setDuration(delay);
            after(anim);
            return this;
        }
    }

    private static class Dependency {
        static final int AFTER = 1;
        static final int WITH = 0;
        public Node node;
        public int rule;

        public Dependency(Node node, int rule) {
            this.node = node;
            this.rule = rule;
        }
    }

    private static class Node implements Cloneable {
        public Animator10 animation;
        public ArrayList<Dependency> dependencies = null;
        public boolean done = false;
        public ArrayList<Node> nodeDependencies = null;
        public ArrayList<Node> nodeDependents = null;
        public ArrayList<Dependency> tmpDependencies = null;

        public Node(Animator10 animation) {
            this.animation = animation;
        }

        public void addDependency(Dependency dependency) {
            if (this.dependencies == null) {
                this.dependencies = new ArrayList();
                this.nodeDependencies = new ArrayList();
            }
            this.dependencies.add(dependency);
            if (!this.nodeDependencies.contains(dependency.node)) {
                this.nodeDependencies.add(dependency.node);
            }
            Node dependencyNode = dependency.node;
            if (dependencyNode.nodeDependents == null) {
                dependencyNode.nodeDependents = new ArrayList();
            }
            dependencyNode.nodeDependents.add(this);
        }

        public Node clone() {
            try {
                Node node = (Node) super.clone();
                node.animation = this.animation.clone();
                return node;
            } catch (CloneNotSupportedException e) {
                throw new AssertionError();
            }
        }
    }

    private class AnimatorSetListener implements AnimatorListener {
        private AnimatorSet10 mAnimatorSet;

        AnimatorSetListener(AnimatorSet10 animatorSet) {
            this.mAnimatorSet = animatorSet;
        }

        public void onAnimationCancel(Animator10 animation) {
            if (!AnimatorSet10.this.mTerminated && AnimatorSet10.this.mPlayingSet.size() == 0 && AnimatorSet10.this.mListeners != null) {
                int numListeners = AnimatorSet10.this.mListeners.size();
                Iterator i$ = AnimatorSet10.this.mListeners.iterator();
                while (i$.hasNext()) {
                    ((AnimatorListener) i$.next()).onAnimationCancel(this.mAnimatorSet);
                }
            }
        }

        public void onAnimationEnd(Animator10 animation) {
            animation.removeListener(this);
            AnimatorSet10.this.mPlayingSet.remove(animation);
            ((Node) this.mAnimatorSet.mNodeMap.get(animation)).done = true;
            if (!AnimatorSet10.this.mTerminated) {
                ArrayList<Node> sortedNodes = this.mAnimatorSet.mSortedNodes;
                boolean allDone = true;
                int numSortedNodes = sortedNodes.size();
                Iterator i$ = sortedNodes.iterator();
                while (i$.hasNext()) {
                    if (!((Node) i$.next()).done) {
                        allDone = false;
                        break;
                    }
                }
                if (allDone) {
                    if (AnimatorSet10.this.mListeners != null) {
                        ArrayList<AnimatorListener> tmpListeners = (ArrayList) AnimatorSet10.this.mListeners.clone();
                        int numListeners = tmpListeners.size();
                        i$ = tmpListeners.iterator();
                        while (i$.hasNext()) {
                            ((AnimatorListener) i$.next()).onAnimationEnd(this.mAnimatorSet);
                        }
                    }
                    this.mAnimatorSet.mStarted = false;
                    this.mAnimatorSet.mPaused = false;
                }
            }
        }

        public void onAnimationRepeat(Animator10 animation) {
        }

        public void onAnimationStart(Animator10 animation) {
        }
    }

    private static class DependencyListener implements AnimatorListener {
        private AnimatorSet10 mAnimatorSet;
        private Node mNode;
        private int mRule;

        public DependencyListener(AnimatorSet10 animatorSet, Node node, int rule) {
            this.mAnimatorSet = animatorSet;
            this.mNode = node;
            this.mRule = rule;
        }

        public void onAnimationCancel(Animator10 animation) {
        }

        public void onAnimationEnd(Animator10 animation) {
            if (this.mRule == 1) {
                startIfReady(animation);
            }
        }

        public void onAnimationRepeat(Animator10 animation) {
        }

        public void onAnimationStart(Animator10 animation) {
            if (this.mRule == 0) {
                startIfReady(animation);
            }
        }

        private void startIfReady(Animator10 dependencyAnimation) {
            if (!this.mAnimatorSet.mTerminated) {
                Dependency dependencyToRemove = null;
                int numDependencies = this.mNode.tmpDependencies.size();
                for (int i = 0; i < numDependencies; i++) {
                    Dependency dependency = (Dependency) this.mNode.tmpDependencies.get(i);
                    if (dependency.rule == this.mRule && dependency.node.animation == dependencyAnimation) {
                        dependencyToRemove = dependency;
                        dependencyAnimation.removeListener(this);
                        break;
                    }
                }
                this.mNode.tmpDependencies.remove(dependencyToRemove);
                if (this.mNode.tmpDependencies.size() == 0) {
                    this.mNode.animation.start();
                    this.mAnimatorSet.mPlayingSet.add(this.mNode.animation);
                }
            }
        }
    }

    public void playTogether(Animator10... items) {
        if (items != null) {
            this.mNeedsSort = true;
            Builder builder = play(items[0]);
            for (int i = 1; i < items.length; i++) {
                builder.with(items[i]);
            }
        }
    }

    public void playTogether(Collection<Animator10> items) {
        if (items != null && items.size() > 0) {
            this.mNeedsSort = true;
            Builder builder = null;
            for (Animator10 anim : items) {
                if (builder == null) {
                    builder = play(anim);
                } else {
                    builder.with(anim);
                }
            }
        }
    }

    public void playSequentially(Animator10... items) {
        if (items != null) {
            this.mNeedsSort = true;
            if (items.length == 1) {
                play(items[0]);
                return;
            }
            for (int i = 0; i < items.length - 1; i++) {
                play(items[i]).before(items[i + 1]);
            }
        }
    }

    public void playSequentially(List<Animator10> items) {
        if (items != null && items.size() > 0) {
            this.mNeedsSort = true;
            if (items.size() == 1) {
                play((Animator10) items.get(0));
                return;
            }
            for (int i = 0; i < items.size() - 1; i++) {
                play((Animator10) items.get(i)).before((Animator10) items.get(i + 1));
            }
        }
    }

    public ArrayList<Animator10> getChildAnimations() {
        ArrayList<Animator10> childList = new ArrayList();
        Iterator i$ = this.mNodes.iterator();
        while (i$.hasNext()) {
            childList.add(((Node) i$.next()).animation);
        }
        return childList;
    }

    public void setTarget(Object target) {
        Iterator i$ = this.mNodes.iterator();
        while (i$.hasNext()) {
            Animator10 animation = ((Node) i$.next()).animation;
            if (animation instanceof AnimatorSet10) {
                animation.setTarget(target);
            } else if (animation instanceof ObjectAnimator10) {
                animation.setTarget(target);
            }
        }
    }

    public void setInterpolator(Interpolator interpolator) {
        this.mInterpolator = interpolator;
    }

    public Interpolator getInterpolator() {
        return this.mInterpolator;
    }

    public Builder play(Animator10 anim) {
        if (anim == null) {
            return null;
        }
        this.mNeedsSort = true;
        return new Builder(anim);
    }

    public void cancel() {
        this.mTerminated = true;
        if (isStarted()) {
            Iterator i$;
            ArrayList<AnimatorListener> tmpListeners = null;
            if (this.mListeners != null) {
                tmpListeners = (ArrayList) this.mListeners.clone();
                i$ = tmpListeners.iterator();
                while (i$.hasNext()) {
                    ((AnimatorListener) i$.next()).onAnimationCancel(this);
                }
            }
            if (this.mDelayAnim != null && this.mDelayAnim.isRunning()) {
                this.mDelayAnim.cancel();
            } else if (this.mSortedNodes.size() > 0) {
                i$ = this.mSortedNodes.iterator();
                while (i$.hasNext()) {
                    ((Node) i$.next()).animation.cancel();
                }
            }
            if (tmpListeners != null) {
                i$ = tmpListeners.iterator();
                while (i$.hasNext()) {
                    ((AnimatorListener) i$.next()).onAnimationEnd(this);
                }
            }
            this.mStarted = false;
        }
    }

    public void end() {
        this.mTerminated = true;
        if (isStarted()) {
            Iterator i$;
            if (this.mSortedNodes.size() != this.mNodes.size()) {
                sortNodes();
                i$ = this.mSortedNodes.iterator();
                while (i$.hasNext()) {
                    Node node = (Node) i$.next();
                    if (this.mSetListener == null) {
                        this.mSetListener = new AnimatorSetListener(this);
                    }
                    node.animation.addListener(this.mSetListener);
                }
            }
            if (this.mDelayAnim != null) {
                this.mDelayAnim.cancel();
            }
            if (this.mSortedNodes.size() > 0) {
                i$ = this.mSortedNodes.iterator();
                while (i$.hasNext()) {
                    ((Node) i$.next()).animation.end();
                }
            }
            if (this.mListeners != null) {
                i$ = ((ArrayList) this.mListeners.clone()).iterator();
                while (i$.hasNext()) {
                    ((AnimatorListener) i$.next()).onAnimationEnd(this);
                }
            }
            this.mStarted = false;
        }
    }

    public boolean isRunning() {
        Iterator i$ = this.mNodes.iterator();
        while (i$.hasNext()) {
            if (((Node) i$.next()).animation.isRunning()) {
                return true;
            }
        }
        return false;
    }

    public boolean isStarted() {
        return this.mStarted;
    }

    public long getStartDelay() {
        return this.mStartDelay;
    }

    public void setStartDelay(long startDelay) {
        this.mStartDelay = startDelay;
    }

    public long getDuration() {
        return this.mDuration;
    }

    public AnimatorSet10 setDuration(long duration) {
        if (duration < 0) {
            throw new IllegalArgumentException("duration must be a value of zero or greater");
        }
        this.mDuration = duration;
        return this;
    }

    public void setupStartValues() {
        Iterator i$ = this.mNodes.iterator();
        while (i$.hasNext()) {
            ((Node) i$.next()).animation.setupStartValues();
        }
    }

    public void setupEndValues() {
        Iterator i$ = this.mNodes.iterator();
        while (i$.hasNext()) {
            ((Node) i$.next()).animation.setupEndValues();
        }
    }

    public void pause() {
        boolean previouslyPaused = this.mPaused;
        super.pause();
        if (!previouslyPaused && this.mPaused) {
            if (this.mDelayAnim != null) {
                this.mDelayAnim.pause();
                return;
            }
            Iterator i$ = this.mNodes.iterator();
            while (i$.hasNext()) {
                ((Node) i$.next()).animation.pause();
            }
        }
    }

    public void resume() {
        boolean previouslyPaused = this.mPaused;
        super.resume();
        if (previouslyPaused && !this.mPaused) {
            if (this.mDelayAnim != null) {
                this.mDelayAnim.resume();
                return;
            }
            Iterator i$ = this.mNodes.iterator();
            while (i$.hasNext()) {
                ((Node) i$.next()).animation.resume();
            }
        }
    }

    public void start() {
        Iterator i$;
        this.mTerminated = false;
        this.mStarted = true;
        this.mPaused = false;
        if (this.mDuration >= 0) {
            i$ = this.mNodes.iterator();
            while (i$.hasNext()) {
                ((Node) i$.next()).animation.setDuration(this.mDuration);
            }
        }
        if (this.mInterpolator != null) {
            i$ = this.mNodes.iterator();
            while (i$.hasNext()) {
                ((Node) i$.next()).animation.setInterpolator(this.mInterpolator);
            }
        }
        sortNodes();
        int numSortedNodes = this.mSortedNodes.size();
        i$ = this.mSortedNodes.iterator();
        while (i$.hasNext()) {
            Node node = (Node) i$.next();
            ArrayList<AnimatorListener> oldListeners = node.animation.getListeners();
            if (oldListeners != null && oldListeners.size() > 0) {
                Iterator i$2 = new ArrayList(oldListeners).iterator();
                while (i$2.hasNext()) {
                    AnimatorListener listener = (AnimatorListener) i$2.next();
                    if ((listener instanceof DependencyListener) || (listener instanceof AnimatorSetListener)) {
                        node.animation.removeListener(listener);
                    }
                }
            }
        }
        final ArrayList<Node> nodesToStart = new ArrayList();
        i$ = this.mSortedNodes.iterator();
        while (i$.hasNext()) {
            node = (Node) i$.next();
            if (this.mSetListener == null) {
                this.mSetListener = new AnimatorSetListener(this);
            }
            if (node.dependencies == null || node.dependencies.size() == 0) {
                nodesToStart.add(node);
            } else {
                int numDependencies = node.dependencies.size();
                for (int j = 0; j < numDependencies; j++) {
                    Dependency dependency = (Dependency) node.dependencies.get(j);
                    dependency.node.animation.addListener(new DependencyListener(this, node, dependency.rule));
                }
                node.tmpDependencies = (ArrayList) node.dependencies.clone();
            }
            node.animation.addListener(this.mSetListener);
        }
        if (this.mStartDelay <= 0) {
            i$ = nodesToStart.iterator();
            while (i$.hasNext()) {
                node = (Node) i$.next();
                node.animation.start();
                this.mPlayingSet.add(node.animation);
            }
        } else {
            float[] fArr = new float[2];
            this.mDelayAnim = ValueAnimator.ofFloat(0.0f, 1.0f);
            this.mDelayAnim.setDuration(this.mStartDelay);
            this.mDelayAnim.addListener(new AnimatorListenerAdapter10() {
                boolean canceled = false;

                public void onAnimationCancel(Animator10 anim) {
                    this.canceled = true;
                }

                public void onAnimationEnd(Animator10 anim) {
                    if (!this.canceled) {
                        int numNodes = nodesToStart.size();
                        Iterator i$ = nodesToStart.iterator();
                        while (i$.hasNext()) {
                            Node node = (Node) i$.next();
                            node.animation.start();
                            AnimatorSet10.this.mPlayingSet.add(node.animation);
                        }
                    }
                    AnimatorSet10.this.mDelayAnim = null;
                }
            });
            this.mDelayAnim.start();
        }
        if (this.mListeners != null) {
            ArrayList<AnimatorListener> tmpListeners = (ArrayList) this.mListeners.clone();
            int numListeners = tmpListeners.size();
            i$ = tmpListeners.iterator();
            while (i$.hasNext()) {
                ((AnimatorListener) i$.next()).onAnimationStart(this);
            }
        }
        if (this.mNodes.size() == 0 && this.mStartDelay == 0) {
            this.mStarted = false;
            if (this.mListeners != null) {
                tmpListeners = (ArrayList) this.mListeners.clone();
                numListeners = tmpListeners.size();
                i$ = tmpListeners.iterator();
                while (i$.hasNext()) {
                    ((AnimatorListener) i$.next()).onAnimationEnd(this);
                }
            }
        }
    }

    public AnimatorSet10 clone() {
        Iterator i$;
        AnimatorSet10 anim = (AnimatorSet10) super.clone();
        anim.mNeedsSort = true;
        anim.mTerminated = false;
        anim.mStarted = false;
        anim.mPlayingSet = new ArrayList();
        anim.mNodeMap = new HashMap();
        anim.mNodes = new ArrayList();
        anim.mSortedNodes = new ArrayList();
        HashMap<Node, Node> nodeCloneMap = new HashMap();
        Iterator it = this.mNodes.iterator();
        while (it.hasNext()) {
            Node node = (Node) it.next();
            Node nodeClone = node.clone();
            nodeCloneMap.put(node, nodeClone);
            anim.mNodes.add(nodeClone);
            anim.mNodeMap.put(nodeClone.animation, nodeClone);
            nodeClone.dependencies = null;
            nodeClone.tmpDependencies = null;
            nodeClone.nodeDependents = null;
            nodeClone.nodeDependencies = null;
            ArrayList<AnimatorListener> cloneListeners = nodeClone.animation.getListeners();
            if (cloneListeners != null) {
                ArrayList<AnimatorListener> listenersToRemove = null;
                i$ = cloneListeners.iterator();
                while (i$.hasNext()) {
                    AnimatorListener listener = (AnimatorListener) i$.next();
                    if (listener instanceof AnimatorSetListener) {
                        if (listenersToRemove == null) {
                            listenersToRemove = new ArrayList();
                        }
                        listenersToRemove.add(listener);
                    }
                }
                if (listenersToRemove != null) {
                    i$ = listenersToRemove.iterator();
                    while (i$.hasNext()) {
                        cloneListeners.remove((AnimatorListener) i$.next());
                    }
                }
            }
        }
        it = this.mNodes.iterator();
        while (it.hasNext()) {
            node = (Node) it.next();
            nodeClone = (Node) nodeCloneMap.get(node);
            if (node.dependencies != null) {
                i$ = node.dependencies.iterator();
                while (i$.hasNext()) {
                    Dependency dependency = (Dependency) i$.next();
                    nodeClone.addDependency(new Dependency((Node) nodeCloneMap.get(dependency.node), dependency.rule));
                }
            }
        }
        return anim;
    }

    private void sortNodes() {
        Iterator i$;
        Node node;
        int j;
        if (this.mNeedsSort) {
            this.mSortedNodes.clear();
            ArrayList<Node> roots = new ArrayList();
            int numNodes = this.mNodes.size();
            i$ = this.mNodes.iterator();
            while (i$.hasNext()) {
                node = (Node) i$.next();
                if (node.dependencies == null || node.dependencies.size() == 0) {
                    roots.add(node);
                }
            }
            ArrayList<Node> tmpRoots = new ArrayList();
            while (roots.size() > 0) {
                int numRoots = roots.size();
                i$ = roots.iterator();
                while (i$.hasNext()) {
                    Node root = (Node) i$.next();
                    this.mSortedNodes.add(root);
                    if (root.nodeDependents != null) {
                        int numDependents = root.nodeDependents.size();
                        for (j = 0; j < numDependents; j++) {
                            node = (Node) root.nodeDependents.get(j);
                            node.nodeDependencies.remove(root);
                            if (node.nodeDependencies.size() == 0) {
                                tmpRoots.add(node);
                            }
                        }
                    }
                }
                roots.clear();
                roots.addAll(tmpRoots);
                tmpRoots.clear();
            }
            this.mNeedsSort = false;
            if (this.mSortedNodes.size() != this.mNodes.size()) {
                throw new IllegalStateException("Circular dependencies cannot exist in AnimatorSet");
            }
            return;
        }
        numNodes = this.mNodes.size();
        i$ = this.mNodes.iterator();
        while (i$.hasNext()) {
            node = (Node) i$.next();
            if (node.dependencies != null && node.dependencies.size() > 0) {
                int numDependencies = node.dependencies.size();
                for (j = 0; j < numDependencies; j++) {
                    Dependency dependency = (Dependency) node.dependencies.get(j);
                    if (node.nodeDependencies == null) {
                        node.nodeDependencies = new ArrayList();
                    }
                    if (!node.nodeDependencies.contains(dependency.node)) {
                        node.nodeDependencies.add(dependency.node);
                    }
                }
            }
            node.done = false;
        }
    }
}
