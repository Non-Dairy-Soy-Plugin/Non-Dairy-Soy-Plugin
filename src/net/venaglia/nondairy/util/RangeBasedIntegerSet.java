/*
 * Copyright 2011 Ed Venaglia
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package net.venaglia.nondairy.util;

//import junit.framework.TestCase;

import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.ConcurrentModificationException;
import java.util.NoSuchElementException;

/**
 * Utility class that contains integers as a set of ranges. While this set may
 * report an enormous size, it is not likely that it will consume a large
 * amount of memeory if the values are mostly consecutive.
 */
public class RangeBasedIntegerSet implements Iterable<Integer>
{
  List<Range> _liAllRanges;
  long _lSize;
  int _iModCount = 0;

  public RangeBasedIntegerSet ()
  {
    _liAllRanges = new ArrayList<Range>();
    _liAllRanges.add(new Range(false, Integer.MIN_VALUE, Integer.MAX_VALUE));
    _lSize = 0;
  }

  public RangeBasedIntegerSet (RangeBasedIntegerSet oIntSet)
  {
    _liAllRanges = new ArrayList<Range>(oIntSet._liAllRanges.size());
    for (int i = 0; i < oIntSet._liAllRanges.size(); i++)
    {
      Range oRange = oIntSet._liAllRanges.get(i);
      _liAllRanges.add(new Range(oRange.isIncluded(),
                                 oRange.getBegin(),
                                 oRange.getEnd()));
    }
    _lSize = oIntSet._lSize;
  }

  private void valdateFromTo (int iFrom, int iTo)
  {
    if (iFrom > iTo)
    {
      throw new IllegalArgumentException("The \"from\" value must be less " +
                                         "than or equal to the \"to\" value");
    }
  }

  private Range findRange (int iValue)
  {
    Range oRange = null;
    int iLow = 0;
    int iHigh = _liAllRanges.size() - 1;
    while (iLow <= iHigh)
    {
      int iMid = (iLow + iHigh) >> 1;
      Range oMidRange = _liAllRanges.get(iMid);
      int iCmp = oMidRange.compareTo(iValue);

      if (iCmp < 0)
      {
        iLow = iMid + 1;
      }
      else if (iCmp > 0)
      {
        iHigh = iMid - 1;
      }
      else
      {
        oMidRange.setIndex(iMid);
        oRange = oMidRange;
        break;
      }
    }
//    if (oRange == null)
//    {
//      throw new RuntimeException("Binary search failed. findRange() should " +
//                                 "ALWAYS find something.");
//    }
    return oRange;
  }

  private void alterRanges (Range[] arNewRanges)
  {
    int iBaseIndex = -1;
    int iNewRangeIndex = -1;
    for (int i = 0; i < arNewRanges.length; i++)
    {
      Range oNewRange = arNewRanges[i];
      if (oNewRange.getIndex() >= 0)
      {
//        if (iNewRangeIndex >= 0)
//        {
//          // more than one, abort
//          iNewRangeIndex = -1;
//          iBaseIndex = -1;
//          break;
//        }
        iNewRangeIndex = i;
        iBaseIndex = oNewRange.getIndex();
      }
    }
//    if (iBaseIndex < 0)
//    {
//      throw new RuntimeException("alterRanges() only valid when there is " +
//                                 "exactly one existing range in the array.");
//    }
    for (int i = 0; i < arNewRanges.length; i++)
    {
      Range oNewRange = arNewRanges[i];
      int iInsertIndex = iBaseIndex + i;
      oNewRange.setIndex(iInsertIndex);
      if (i != iNewRangeIndex)
      {
        _liAllRanges.add(iInsertIndex, oNewRange);
        _lSize += oNewRange.size();
      }
    }
    // remove any following ranges that are overlapped by newly inserted ranges.
    {
      int iTestIndex = iBaseIndex + arNewRanges.length - 1;
      Range oThisRange = _liAllRanges.get(iTestIndex);
      boolean boDoneRemoving = (iTestIndex + 1) >= _liAllRanges.size();
      while (!boDoneRemoving)
      {
        Range oNextRange = _liAllRanges.get(iTestIndex + 1);
        if ((oThisRange.getEnd() + 1) == oNextRange.getBegin())
        {
          boDoneRemoving = true;
        }
        else
        {
          RangeBasedIntegerSet.Range oRange
                  = _liAllRanges.remove(iTestIndex + 1);
          _lSize -= oRange.size();
          if (iTestIndex + 1 >= _liAllRanges.size())
          {
            boDoneRemoving = true;
          }
        }
      }
    }
    for (int i = iBaseIndex + arNewRanges.length - 1; i >= (iBaseIndex - 1); i--)
    {
      if ((i >= 0) && (i + 1 < _liAllRanges.size()))
      {
        Range oThisRange = _liAllRanges.get(i);
        Range oNextRange = _liAllRanges.get(i + 1);
        if ((oThisRange.isIncluded() == oNextRange.isIncluded()) &&
            ((oThisRange.getEnd() + 1) == oNextRange.getBegin()))
        {
          // consolidate these adjacent ranges, size is not modified here.
          oNextRange.setBegin(oThisRange.getBegin());
          _liAllRanges.remove(i);
        }
      }
    }
  }

  public boolean isEmpty ()
  {
    return _lSize == 0;
  }

  public boolean contains (int iValue)
  {
    return findRange(iValue).isIncluded();
  }

  public boolean add (int iValue)
  {
    Range oRange = findRange(iValue);
    boolean boChanged = false;
    if (!oRange.isIncluded())
    {
      Range oNewRange = new Range(true, iValue, iValue);
      Range[] arModifyRanges;
      if ((oRange.getBegin() == iValue) && (oRange.getEnd() == iValue))
      {
        _lSize += oRange.setIncluded(true);
        arModifyRanges = new Range[]{
                oRange
        };
      }
      else if (oRange.getBegin() == iValue)
      {
        _lSize += oRange.setBegin(iValue + 1);
        arModifyRanges = new Range[]{
                oNewRange,
                oRange
        };
      }
      else if (oRange.getEnd() == iValue)
      {
        _lSize += oRange.setEnd(iValue - 1);
        arModifyRanges = new Range[]{
                oRange,
                oNewRange
        };
      }
      else
      {
        int iEndValue = oRange.getEnd();
        _lSize += oRange.setEnd(iValue - 1);
        arModifyRanges = new Range[]{
                oRange,
                oNewRange,
                new Range(false, iValue + 1, iEndValue)
        };
      }
      alterRanges(arModifyRanges);
      _iModCount++;
      boChanged = true;
    }
    return boChanged;
  }

  public boolean remove (int iValue)
  {
    Range oRange = findRange(iValue);
    boolean boChanged = false;
    if (oRange.isIncluded())
    {
      Range[] arModifyRanges;
      if ((oRange.getBegin() == iValue) && (oRange.getEnd() == iValue))
      {
        _lSize += oRange.setIncluded(false);
        arModifyRanges = new Range[]{
                oRange
        };
      }
      else if (oRange.getBegin() == iValue)
      {
        _lSize += oRange.setBegin(iValue + 1);
        arModifyRanges = new Range[]{
                new Range(false, iValue, iValue),
                oRange
        };
      }
      else if (oRange.getEnd() == iValue)
      {
        _lSize += oRange.setEnd(iValue - 1);
        arModifyRanges = new Range[]{
                oRange,
                new Range(false, iValue, iValue)
        };
      }
      else
      {
        int iEndValue = oRange.getEnd();
        _lSize += oRange.setEnd(iValue - 1);
        arModifyRanges = new Range[]{
                oRange,
                new Range(false, iValue, iValue),
                new Range(true, iValue + 1, iEndValue)
        };
      }
      alterRanges(arModifyRanges);
      _iModCount++;
      boChanged = true;
    }
    return boChanged;
  }

  public boolean containsAll (int iFrom, int iTo)
  {
    valdateFromTo(iFrom, iTo);
    Range oRange = findRange(iFrom);
    return oRange.isIncluded() && oRange.contains(iTo);
  }

  public boolean containsAll (RangeBasedIntegerSet oValues)
  {
    boolean boContainsAll = true;
    for (int i = 0; i < oValues._liAllRanges.size(); i++)
    {
      Range oRange = oValues._liAllRanges.get(i);
      if (oRange.isIncluded() &&
          (!containsAll(oRange.getBegin(), oRange.getEnd())))
      {
        boContainsAll = false;
        break;
      }
    }
    return boContainsAll;
  }

  public boolean containsAny (int iFrom, int iTo)
  {
    try
    {
      int iValue = getNext(iFrom);
      return iValue <= iTo;
    }
    catch (NoSuchElementException e)
    {
      return false;
    }
  }

  public boolean containsAny (RangeBasedIntegerSet oValues)
  {
    boolean boContainsAny = false;
    for (int i = 0; i < oValues._liAllRanges.size(); i++)
    {
      Range oRange = oValues._liAllRanges.get(i);
      if (oRange.isIncluded() &&
          (containsAny(oRange.getBegin(), oRange.getEnd())))
      {
        boContainsAny = true;
        break;
      }
    }
    return boContainsAny;
  }

  public int getNext (int iValue) throws NoSuchElementException
  {
    Range oRange = findRange(iValue);
    if ((!oRange.isIncluded()) && (oRange.getEnd() == Integer.MAX_VALUE))
    {
      throw new NoSuchElementException();
    }
    return oRange.isIncluded() ? iValue : oRange.getEnd() + 1;
  }

  public int getNextNotIncluded (int iValue) throws NoSuchElementException
  {
    Range oRange = findRange(iValue);
    if (oRange.isIncluded() && (oRange.getEnd() == Integer.MAX_VALUE))
    {
      throw new NoSuchElementException();
    }
    return oRange.isIncluded() ? oRange.getEnd() + 1 : iValue;
  }

  public boolean addAll (int iFrom, int iTo)
  {
    valdateFromTo(iFrom, iTo);
    Range oFirstRange = findRange(iFrom);
    Range oLastRange = findRange(iTo);
    Range[] arModifyRanges = null;
    if (oFirstRange.isIncluded() &&
        oLastRange.isIncluded())
    {
      if (oFirstRange.getIndex() == oLastRange.getIndex())
      {
        // nothing to do, already included
      }
      else
      {
        _lSize += oFirstRange.setEnd(oLastRange.getEnd());
        arModifyRanges = new Range[]{
                oFirstRange
        };
        alterRanges(arModifyRanges);
        _iModCount++;
      }
    }
    else if (oLastRange.isIncluded())
    {
      if (oFirstRange.getBegin() == iFrom)
      {
        _lSize += oFirstRange.setIncluded(true);
        _lSize += oFirstRange.setEnd(oLastRange.getEnd());
        arModifyRanges = new Range[]{
                oFirstRange
        };
        alterRanges(arModifyRanges);
        _iModCount++;
      }
      else
      {
        _lSize += oFirstRange.setEnd(iFrom - 1);
        Range oNewRange = new Range(true, iFrom, oLastRange.getBegin() - 1);
        arModifyRanges = new Range[]{
                oFirstRange,
                oNewRange
        };
        alterRanges(arModifyRanges);
        _iModCount++;
      }
    }
    else if (oFirstRange.isIncluded())
    {
      _lSize += oFirstRange.setEnd(iTo);
      if (oLastRange.getEnd() != iTo)
      {
        oLastRange.setBegin(iTo + 1);
      }
      arModifyRanges = new Range[]{
              oFirstRange
      };
      alterRanges(arModifyRanges);
      _iModCount++;
    }
    else if ((oFirstRange.getBegin() == iFrom) && (oLastRange.getEnd() == iTo))
    {
      _lSize += oFirstRange.setIncluded(true);
      _lSize += oFirstRange.setEnd(iTo);
      arModifyRanges = new Range[]{
              oFirstRange
      };
      alterRanges(arModifyRanges);
      _iModCount++;
    }
    else if (oFirstRange.getBegin() == iFrom)
    {
      Range oNewRange = new Range(false, iTo + 1, oLastRange.getEnd());
      _lSize += oFirstRange.setIncluded(true);
      _lSize += oFirstRange.setEnd(iTo);
      arModifyRanges = new Range[]{
              oFirstRange,
              oNewRange
      };
      alterRanges(arModifyRanges);
      _iModCount++;
    }
    else if (oLastRange.getEnd() == iTo)
    {
      Range oNewRange = new Range(true, iFrom, iTo);
      _lSize += oFirstRange.setEnd(iFrom - 1);
      arModifyRanges = new Range[]{
              oFirstRange,
              oNewRange
      };
      alterRanges(arModifyRanges);
      _iModCount++;
    }
    else
    {
      if (oFirstRange.getIndex() == oLastRange.getIndex())
      {
        Range oNewRange = new Range(true, iFrom, iTo);
        Range oNewRange2 = new Range(false, iTo + 1, oFirstRange.getEnd());
        _lSize += oFirstRange.setEnd(iFrom - 1);
        arModifyRanges = new Range[]{
                oFirstRange,
                oNewRange,
                oNewRange2
        };
        alterRanges(arModifyRanges);
        _iModCount++;
      }
      else
      {
        Range oNewRange = new Range(true, iFrom, iTo);
        _lSize += oFirstRange.setEnd(iFrom - 1);
        _lSize += oLastRange.setBegin(iTo + 1);
        arModifyRanges = new Range[]{
                oFirstRange,
                oNewRange
        };
        alterRanges(arModifyRanges);
        _iModCount++;
      }
    }
    return arModifyRanges != null;
  }

  public boolean addAll (RangeBasedIntegerSet oValues)
  {
    boolean boChanged = false;
    for (int i = 0; i < oValues._liAllRanges.size(); i++)
    {
      Range oRange = oValues._liAllRanges.get(i);
      if (oRange.isIncluded())
      {
        boChanged |= addAll(oRange.getBegin(), oRange.getEnd());
      }
    }
    return boChanged;
  }

  public boolean retainAll (int iFrom, int iTo)
  {
    valdateFromTo(iFrom, iTo);
    RangeBasedIntegerSet oTempSet = new RangeBasedIntegerSet();
    oTempSet.addAll(iFrom, iTo);
    return retainAll(oTempSet);
  }

  public boolean retainAll (RangeBasedIntegerSet oValues)
  {
    boolean boChanged = false;
    List<Range> liRanges = new ArrayList<Range>(_liAllRanges.size());
    Iterator<Range> itLeftRanges = _liAllRanges.iterator();
    Iterator<Range> itRightRanges = oValues._liAllRanges.iterator();
    Range oLeft = itLeftRanges.next();
    Range oRight = itRightRanges.next();
    boolean boDone = false;
    while (!boDone)
    {
      // This loops increments based on the left side;
      // left and right will always begin at the same value.
      boChanged |= (oLeft.isIncluded() != oRight.isIncluded());
      if (oLeft.getEnd() == oRight.getEnd())
      {
        liRanges.add(new Range(oLeft.isIncluded() && oRight.isIncluded(),
                               oLeft.getBegin(),
                               oLeft.getEnd()));
        if (itLeftRanges.hasNext())
        {
          oLeft = itLeftRanges.next();
          oRight = itRightRanges.next();
        }
        else
        {
          boDone = true;
        }
      }
      else if (oLeft.getEnd() < oRight.getEnd())
      {
        liRanges.add(new Range(oLeft.isIncluded() && oRight.isIncluded(),
                               oLeft.getBegin(),
                               oLeft.getEnd()));
        oRight = new Range(oRight.isIncluded(),
                           oLeft.getEnd() + 1,
                           oRight.getEnd());
        oLeft = itLeftRanges.next();
      }
      else if (oLeft.getEnd() > oRight.getEnd())
      {
        liRanges.add(new Range(oLeft.isIncluded() && oRight.isIncluded(),
                               oLeft.getBegin(),
                               oRight.getEnd()));
        oLeft = new Range(oLeft.isIncluded(),
                          oRight.getEnd() + 1,
                          oLeft.getEnd());
        oRight = itRightRanges.next();
      }
    }

    if (boChanged)
    {
      // Consolidate and optimize ranges.
      Iterator<Range> itRanges = liRanges.iterator();
      Range oLeftRange = itRanges.next();
      long lSize = oLeftRange.size();
      while (itRanges.hasNext())
      {
        Range oRightRange = itRanges.next();
        lSize += oRightRange.size();
        if (oRightRange.isIncluded() == oLeftRange.isIncluded())
        {
          oLeftRange.setEnd(oRightRange.getEnd());
          itRanges.remove();
        }
        else
        {
          oLeftRange = oRightRange;
        }
      }
      _liAllRanges.clear();
      _liAllRanges = liRanges;
      _lSize = lSize;
      _iModCount++;
    }
    return boChanged;
  }

  public boolean removeAll (int iFrom, int iTo)
  {
    valdateFromTo(iFrom, iTo);
    Range oFirstRange = findRange(iFrom);
    Range oLastRange = findRange(iTo);
    Range[] arModifyRanges = null;
    if (!oFirstRange.isIncluded() &&
        !oLastRange.isIncluded())
    {
      if (oFirstRange.getIndex() == oLastRange.getIndex())
      {
        // nothing to do, already not included
      }
      else
      {
        _lSize += oFirstRange.setEnd(oLastRange.getEnd());
        arModifyRanges = new Range[]{
                oFirstRange
        };
      }
    }
    else if (!oLastRange.isIncluded())
    {
      if (oFirstRange.getBegin() == iFrom)
      {
        _lSize += oFirstRange.setIncluded(false);
        _lSize += oFirstRange.setEnd(oLastRange.getEnd());
        arModifyRanges = new Range[]{
                oFirstRange
        };
      }
      else
      {
        _lSize += oFirstRange.setEnd(iFrom - 1);
        Range oNewRange = new Range(false, iFrom, oLastRange.getBegin() - 1);
        arModifyRanges = new Range[]{
                oFirstRange,
                oNewRange
        };
      }
    }
    else if (!oFirstRange.isIncluded())
    {
      _lSize += oFirstRange.setEnd(iTo);
      if (oLastRange.getEnd() != iTo)
      {
        _lSize += oLastRange.setBegin(iTo + 1);
      }
      arModifyRanges = new Range[]{
              oFirstRange
      };
    }
    else if ((oFirstRange.getBegin() == iFrom) && (oLastRange.getEnd() == iTo))
    {
      _lSize += oFirstRange.setIncluded(false);
      _lSize += oFirstRange.setEnd(iTo);
      arModifyRanges = new Range[]{
              oFirstRange
      };
    }
    else if (oFirstRange.getBegin() == iFrom)
    {
      Range oNewRange = new Range(true, iTo + 1, oLastRange.getEnd());
      _lSize += oFirstRange.setIncluded(false);
      _lSize += oFirstRange.setEnd(iTo);
      arModifyRanges = new Range[]{
              oFirstRange,
              oNewRange
      };
    }
    else if (oLastRange.getEnd() == iTo)
    {
      Range oNewRange = new Range(false, iFrom, iTo);
      _lSize += oFirstRange.setEnd(iFrom - 1);
      arModifyRanges = new Range[]{
              oFirstRange,
              oNewRange
      };
    }
    else
    {
      if (oFirstRange.getIndex() == oLastRange.getIndex())
      {
        Range oNewRange = new Range(false, iFrom, iTo);
        Range oNewRange2 = new Range(true, iTo + 1, oFirstRange.getEnd());
        _lSize += oFirstRange.setEnd(iFrom - 1);
        arModifyRanges = new Range[]{
                oFirstRange,
                oNewRange,
                oNewRange2
        };
      }
      else
      {
        Range oNewRange = new Range(false, iFrom, iTo);
        _lSize += oFirstRange.setEnd(iFrom - 1);
        _lSize += oLastRange.setBegin(iTo + 1);
        arModifyRanges = new Range[]{
                oFirstRange,
                oNewRange
        };
      }
    }
    if (arModifyRanges != null)
    {
      alterRanges(arModifyRanges);
      _iModCount++;
    }
    return arModifyRanges != null;
  }

  public boolean removeAll (RangeBasedIntegerSet oValues)
  {
    boolean boChanged = false;
    for (int i = 0; i < oValues._liAllRanges.size(); i++)
    {
      Range oRange = oValues._liAllRanges.get(i);
      if (oRange.isIncluded())
      {
        boChanged |= removeAll(oRange.getBegin(), oRange.getEnd());
      }
    }
    return boChanged;
  }

  public Iterator<Integer> iterator ()
  {
    return iterator(Integer.MIN_VALUE);
  }

  public Iterator<Integer> iterator (int iFirstValue)
  {
    return new IntIterator(iFirstValue);
  }

  public void clear ()
  {
    if (_lSize > 0)
    {
      _liAllRanges.clear();
      _liAllRanges.add(new Range(false, Integer.MIN_VALUE, Integer.MAX_VALUE));
      _lSize = 0;
      _iModCount++;
    }
  }

  public void invert ()
  {
    for (int i = 0; i < _liAllRanges.size(); i++)
    {
      Range oRange = _liAllRanges.get(i);
      _lSize += oRange.setIncluded(!oRange.isIncluded());
    }
    _iModCount++;
  }

  public int size ()
  {
    return (_lSize > ((long)Integer.MAX_VALUE))
           ? Integer.MAX_VALUE
           : ((int)_lSize);
  }

  public boolean equals (Object o)
  {
    if (this == o) return true;
    if (!(o instanceof RangeBasedIntegerSet)) return false;

    RangeBasedIntegerSet oThat = (RangeBasedIntegerSet)o;

    return _liAllRanges.equals(oThat._liAllRanges);

  }

  public int hashCode ()
  {
    return _liAllRanges.hashCode();
  }

  public String toString ()
  {
    StringBuilder sbBuffer = new StringBuilder();
    boolean boFirstRange = true;
    sbBuffer.append('[');
    for (int i = 0; i < _liAllRanges.size(); i++)
    {
      Range oRange = _liAllRanges.get(i);
      if (oRange.isIncluded())
      {
        if (boFirstRange)
        {
          boFirstRange = false;
        }
        else
        {
          sbBuffer.append(',');
        }
        sbBuffer.append(oRange.toString());
      }
    }
    sbBuffer.append(']');
    return sbBuffer.toString();
  }

  @SuppressWarnings({ "CloneDoesntDeclareCloneNotSupportedException" })
  public Object clone ()
  {
    return new RangeBasedIntegerSet(this);
  }

  class Range
  {
    private boolean _boIncluded;
    private int _iBegin; // inclusive
    private int _iEnd;   // inclusive
    private int _iIndex = -1;

    Range (boolean boIncluded, int iBegin, int iEnd)
    {
      _boIncluded = boIncluded;
      _iBegin = iBegin;
      _iEnd = iEnd;
    }

    int compareTo (int iValue)
    {
      return (_iBegin > iValue) ? 1 : ((_iEnd < iValue) ? -1 : 0);
    }

    boolean isIncluded ()
    {
      return _boIncluded;
    }

    long setIncluded (boolean boIncluded)
    {
      long lPreviousSize = size();
      _boIncluded = boIncluded;
      return size() - lPreviousSize;
    }

    int getBegin ()
    {
      return _iBegin;
    }

    long setBegin (int iBegin)
    {
      long lPreviousSize = size();
      _iBegin = iBegin;
      return size() - lPreviousSize;
    }

    int getEnd ()
    {
      return _iEnd;
    }

    long setEnd (int iEnd)
    {
      long lPreviousSize = size();
      _iEnd = iEnd;
      return size() - lPreviousSize;
    }

    /** only valid when obtained immediately after calling findRange(). */
    int getIndex ()
    {
      return _iIndex;
    }

    void setIndex (int iIndex)
    {
      _iIndex = iIndex;
    }

    boolean contains (int iValue)
    {
      return (iValue <= _iEnd) && (iValue >= _iBegin);
    }

    long size ()
    {
      return _boIncluded ? ((long)_iEnd) - ((long)_iBegin) + 1L : 0L;
    }


    public boolean equals (Object o)
    {
      if (this == o) return true;
      if (o == null || getClass() != o.getClass()) return false;

      Range range = (Range)o;

      if (_boIncluded != range._boIncluded) return false;
      if (_iBegin != range._iBegin) return false;
      if (_iEnd != range._iEnd) return false;

      return true;
    }

    public int hashCode ()
    {
      int result;
      result = (_boIncluded ? 1 : 0);
      result = 31 * result + _iBegin;
      result = 31 * result + _iEnd;
      return result;
    }

    public String toString ()
    {
      return (_iBegin == _iEnd)
             ? String.valueOf(_iBegin)
             : String.valueOf(_iBegin) + ".." + String.valueOf(_iEnd);
    }
  }

  class IntIterator implements Iterator<Integer>
  {
    private int _iExpectedModCount = _iModCount;
    private boolean _boNextIsMinValue;
    private boolean _boHasLastValue = false;
    private int _iLastValue;
    private Range _oNextRange = null;

    private IntIterator (int iFirstValue)
    {
      if (iFirstValue == Integer.MIN_VALUE)
      {
        _boNextIsMinValue = true;
        _oNextRange = _liAllRanges.get(0);
        _oNextRange.setIndex(0);
        _iLastValue = Integer.MIN_VALUE;
      }
      else
      {
        _boNextIsMinValue = false;
        _iLastValue = iFirstValue - 1;
      }
    }

    private void checkForComodification ()
    {
      if (_iExpectedModCount != _iModCount)
      {
        throw new ConcurrentModificationException();
      }
    }

    public boolean hasNext ()
    {
      checkForComodification();
      if (!_boHasLastValue && !_boNextIsMinValue)
      {
        _oNextRange = findRange(_iLastValue);
      }
      while ((_oNextRange != null) &&
             ((!_oNextRange.isIncluded()) ||
              (_oNextRange.getEnd() <= _iLastValue)))
      {
        _boNextIsMinValue = false;
        int iIndex = _oNextRange.getIndex() + 1;
        if (iIndex < _liAllRanges.size())
        {
          _oNextRange = _liAllRanges.get(iIndex);
          _oNextRange.setIndex(iIndex);
        }
        else
        {
          _oNextRange = null; // no more values
          _boNextIsMinValue = true; // quickens next call to hasNext();
        }
      }
      return _oNextRange != null;
    }

    public Integer next ()
    {
      checkForComodification();
      if (!hasNext())
      {
        throw new NoSuchElementException();
      }
      _boHasLastValue = true;
      _iLastValue = _boNextIsMinValue
                    ? Integer.MIN_VALUE
                    : _iLastValue < _oNextRange.getBegin()
                      ? _oNextRange.getBegin()
                      : _iLastValue + 1;
      _boNextIsMinValue = false;
      return new Integer(_iLastValue);
    }

    public void remove ()
    {
      checkForComodification();
      if (!_boHasLastValue)
      {
        throw new IllegalStateException();
      }
      RangeBasedIntegerSet.this.remove(_iLastValue);
      _iExpectedModCount = _iModCount;
      _boHasLastValue = false;
    }
  }
}
